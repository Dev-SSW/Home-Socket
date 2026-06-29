# Home-Socket 성능 최적화 정리

Home-Socket은 기존 쇼핑몰 백엔드 구조를 기반으로 기능을 확장하면서, 조회 성능과 동시성 안정성을 함께 개선하는 것을 목표로 최적화를 진행했습니다.

최적화는 단순히 “응답 시간을 줄이는 것”에만 두지 않고, 다음 기준으로 나누어 진행했습니다.

```text
1. 불필요한 쿼리 제거
2. N + 1 문제 완화
3. 조회 API 캐싱
4. 인덱스 적용
5. 동시성 문제 방지
6. 테스트 데이터와 부하 테스트 환경 통제
7. 결제 이후 후속 처리 비동기화
```

---

## 1. 최적화 방향

| 구분 | 적용 방식 |
|---|---|
| 단순 목록 조회 | DTO Projection + Page |
| 단일 엔티티와 연관관계 조회 | EntityGraph 또는 Fetch Join |
| 계층형 카테고리 | BatchSize, 필요한 범위의 자식 카테고리 조회 |
| 반복 조회 API | Redis Cache |
| 데이터 변경 API | Cache Evict |
| 동시 수정 위험 기능 | Pessimistic Lock, Unique 제약 |
| 병목 쿼리 분석 | `EXPLAIN (ANALYZE, BUFFERS)` |
| 성능 비교 | k6 + Node.js 분석 스크립트 |

---

## 2. 쿼리 최적화

### 2.1 User 조회 최적화

유저 조회 과정에서 불필요하게 Cart 조회가 함께 발생하고, 동일 요청에서 User 조회 쿼리가 여러 번 발생하는 문제가 있었습니다.

#### 문제

```text
JwtAuthFilter에서 loadUserByUsername()
→ tokenVersion 확인을 위한 User 조회
→ Service 예외 처리를 위한 User 조회
```

이 흐름으로 인해 User 조회가 최대 3회 발생했습니다.

또한 User와 Cart가 양방향 연관관계를 가지는데, fetch 설정이 명확하지 않아 User 조회 시 Cart 관련 쿼리가 함께 발생했습니다.

#### 해결

- User 엔티티의 Cart 연관관계에 `FetchType.LAZY` 적용
- JwtAuthFilter에서 `loadUserByUsername()`으로 가져온 UserDetails를 활용
- tokenVersion을 별도 조회하지 않고, 이미 로드된 User에서 확인
- 수정/삭제처럼 영속 상태가 필요한 기능에서만 Service 계층에서 User를 재조회

#### 결과

```text
User 조회 쿼리 3회
→ User 조회 쿼리 2회로 감소
```

단, User와 Cart의 양방향 관계 특성상 Cart 존재 여부 확인용 쿼리는 남을 수 있습니다. 이 부분은 인증 전용 Custom UserDetails 또는 인증 DTO를 분리하면 더 줄일 수 있습니다.

---

### 2.2 전체 유저 조회

전체 유저 조회는 단순 목록 데이터를 반환하는 기능이므로 Entity 조회 대신 DTO Projection과 Page를 적용했습니다.

#### 문제

엔티티를 직접 조회하면 각 User마다 Cart 조회가 추가로 발생할 수 있어 `1 + N` 문제가 발생할 수 있습니다.

#### 해결

- Entity 조회 대신 DTO Projection 사용
- Page 적용으로 한 번에 가져오는 데이터 수 제한
- 필요한 컬럼만 조회하여 응답 DTO로 직접 매핑

```text
User Entity 목록 조회
→ UserResponse DTO Projection
```

---

### 2.3 Category 조회 최적화

카테고리는 계층 구조를 가지므로 parent-child 관계 조회 시 N + 1 문제가 발생하기 쉽습니다.

#### 적용 방식

- parent ↔ children 계층 관계에 BatchSize 적용
- 특정 부모의 자식 카테고리 조회 시 필요한 계층만 조회
- 루트 카테고리 조회 시 stream 탐색으로 전체 계층을 순회하지 않도록 조정

#### 개선 방향

```text
루트 카테고리 조회
→ 루트와 1차 자식 카테고리를 쿼리로 조회
→ 이후 필요한 자식은 조건에 맞게 탐색
```

계층형 구조에서는 모든 자식을 한 번에 fetch join하면 데이터 중복과 쿼리 복잡도가 커질 수 있어 BatchSize를 활용했습니다.

---

### 2.4 Item 조회 최적화

상품 조회는 전체 상품 조회와 카테고리별 상품 조회 모두 페이지 단위 조회가 필요했습니다.

#### 적용 방식

- 전체 상품 조회: DTO Projection + Page
- 카테고리별 상품 조회: DTO Projection + Page
- 상품 조회 시 Category와 Item을 한 번에 조회하고 DTO로 변환

```text
Item Entity 조회 후 DTO 변환
→ Repository 단계에서 DTO Projection
```

이 방식으로 불필요한 Entity 로딩을 줄이고, 필요한 데이터만 응답으로 반환하도록 했습니다.

---

### 2.5 Order 조회 최적화

주문 도메인은 User, Delivery, Address, OrderItem, Item, CouponPublish 등 여러 도메인과 연결되어 있어, 무리하게 한 번의 fetch join으로 가져오면 쿼리가 복잡해질 수 있습니다.

#### 주문 상세 조회

단일 주문 상세 조회는 필요한 연관관계를 함께 조회하기 위해 EntityGraph를 사용했습니다.

```text
Order
→ OrderItem
→ Item
→ Delivery
→ Address
```

#### 주문 페이지 조회

주문 페이지는 여러 도메인의 데이터를 조합해야 하므로, 한 번의 복잡한 쿼리 대신 각 도메인별로 필요한 데이터를 조회한 뒤 DTO에서 조합했습니다.

| 데이터 | 조회 방식 |
|---|---|
| Address | DTO Projection |
| CouponPublish | Fetch Join |
| CartItem | Fetch Join |
| OrderItem + Item | 별도 Repository 조회 |
| Delivery + Address | 별도 Repository 조회 |

#### 주문 목록 조회

주문 목록 조회는 Delivery 정도의 단순 연관관계만 필요했기 때문에 DTO Projection으로 처리했습니다.

---

### 2.6 Cart 조회 최적화

장바구니는 Cart, CartItem, Item이 함께 사용되는 경우가 많습니다.

#### 문제

DTO 변환 시 `cart.getCartItems()`가 호출되면서 CartItem과 Item 조회가 추가로 발생할 수 있습니다.

#### 해결

- 장바구니 조회 시 CartItem과 Item을 함께 조회
- 장바구니에 아이템 추가 시 기존 방식인 `cartItems` 순회 대신 `cart + item` 조건으로 CartItem 존재 여부 확인
- 다건 삭제는 IN 절 기반 벌크 삭제 쿼리 사용

```text
Cart 조회
→ CartItem + Item을 함께 조회
→ DTO 변환 시 추가 쿼리 방지
```

---

### 2.7 Address 조회 최적화

주소 생성 시 User 전체 엔티티가 필요한 것이 아니라 FK 참조만 필요합니다.

#### 적용 방식

- 존재 여부 확인은 `exists` 계열 쿼리 사용
- 생성 시에는 proxy 객체 또는 참조 객체 활용
- 조회 응답은 DTO Projection 적용

이 방식으로 단순 생성/조회에서 불필요한 Entity 로딩을 줄였습니다.

---

### 2.8 Review 조회와 평점 계산

리뷰 조회는 특정 리뷰, 유저별 리뷰 목록, 아이템별 리뷰 목록으로 나뉩니다.

#### 적용 방식

- 리뷰 목록 조회에 DTO Projection과 Join 활용
- 생성 시 User, Item은 proxy 참조 활용
- 리뷰 생성/수정/삭제 시 Item 평균 평점 재계산

#### 수정한 문제

첫 리뷰가 등록되는 경우 평균 계산에서 0으로 나누는 문제가 발생할 수 있었습니다.

```text
기존 평균 평점 계산
→ 리뷰 수가 0일 때 0으로 나누는 위험

수정
→ 첫 리뷰는 해당 리뷰의 평점을 그대로 반영
```

---

### 2.9 Coupon 조회 최적화

쿠폰 생성과 발급은 복잡한 연관관계 조회보다 단순 데이터 조회가 중심이므로 DTO Projection을 적용했습니다.

- 쿠폰 생성/조회: DTO Projection
- 쿠폰 발급: User ID 기반 조회
- 발급 쿠폰과 주문 연결 시 필요한 경우에만 연관관계 로딩

---

## 3. 테스트 데이터 통제

성능 비교에서 가장 중요한 것은 동일한 조건에서 테스트하는 것입니다.

Home-Socket에서는 성능 테스트용 데이터 생성 시 고정 seed를 사용해 매번 동일한 데이터 분포가 만들어지도록 했습니다.

```text
같은 테스트 데이터
같은 요청 시나리오
같은 부하 조건
```

이를 통해 캐시, 인덱스, 쿼리 개선 전후 결과를 비교할 수 있도록 했습니다.

---

## 4. k6 부하 테스트

### 테스트 설계

테스트는 read와 write로 분리했습니다.

| 구분 | 이유 |
|---|---|
| Read 테스트 | 여러 번 실행해 평균적인 조회 성능 확인 |
| Write 테스트 | DB 상태를 변경하므로 통제된 조건에서 1회 기준 비교 |
| 공통 모듈 | 요청, 응답 처리, 로그인, 토큰 처리 등 재사용 |
| 분석 스크립트 | before/after 결과 비교 자동화 |

### 실행 기준

각 API는 신뢰성 있는 데이터를 얻기 위해 최소 200회 이상의 요청을 기준으로 설계했습니다.

```text
k6 실행
→ JSON 결과 저장
→ Node.js 분석 스크립트로 p95, 실패율 비교
```

k6 자체 실행에는 Node.js가 필요하지 않지만, 성능 비교 결과를 분석하는 별도 스크립트에는 Node.js를 사용했습니다.

### 대표 결과

| Suite | Before p95 | After p95 | 변화 |
|---|---:|---:|---:|
| read 전체 | 21.49ms | 19.33ms | 약 10.03% 개선 |
| write 전체 | 125.19ms | 73.77ms | 약 41.07% 개선 |

단, 전체 평균만으로 성능이 개선되었다고 판단하지 않고, API별 p95와 실패율을 함께 확인했습니다.

---

## 5. 인덱스 적용

### 적용 배경

성능이 느리다고 판단되는 기능에서 반복적으로 사용되는 조건 컬럼과 정렬 컬럼을 중심으로 인덱스를 검토했습니다.

병목 확인에는 PostgreSQL의 실행 계획 분석을 사용했습니다.

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT ...
```

### 적용 방식

인덱스는 Flyway migration으로 관리했습니다.

```sql
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_item_category_id ON item(category_id);
```

### MySQL에서 PostgreSQL로 전환한 이유

처음에는 MySQL 기반으로도 검토했지만, 인덱스 migration을 반복하며 삭제/재생성/조건부 생성 처리의 복잡성이 커졌습니다.

PostgreSQL은 `CREATE INDEX IF NOT EXISTS`를 지원하므로, migration 재실행이나 환경별 적용에서 더 단순하게 관리할 수 있었습니다.

### 테스트 환경 초기화

버전별 성능 비교 시에는 DB를 초기화하고 동일한 테스트 데이터를 다시 생성했습니다.

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO sgg919;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;
```

특정 Flyway 버전까지만 migration을 적용해 before/after를 비교했습니다.

```bash
java -jar build/libs/practice-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=performance \
  --spring.flyway.target=1
```

---

## 6. Redis 캐시 적용

### 적용 대상

자주 변경되지 않거나 반복 조회가 많은 API에 Redis 캐시를 적용했습니다.

| 대상 | 이유 |
|---|---|
| 루트 카테고리 조회 | 변경 빈도 낮음 |
| 자식 카테고리 조회 | 반복 조회 가능성 높음 |
| 상품 단건 조회 | 상세 페이지 반복 접근 |
| 전체 상품/카테고리별 상품 조회 | 목록 조회 반복 |
| 리뷰 조회 | 상세 페이지에서 반복 조회 가능 |

### 캐시 key 설계

루트 카테고리처럼 고정된 대상은 문자열 key를 사용했습니다.

```java
@Cacheable(value = "getRootCategory", key = "'root'")
```

Page를 사용하는 기능은 page number, page size, sort 조건을 key에 포함했습니다.

```java
key = "#pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()"
```

### Page 직렬화 문제 해결

외부 캐시 저장소인 Redis에 JPA Page 객체를 그대로 저장하면 직렬화 문제가 발생할 수 있습니다.

이를 피하기 위해 Page 응답은 별도 `PageResponse` DTO로 감싸 저장했습니다.

```text
Page<Entity>
→ PageResponse<DTO>
→ Redis 저장
```

### List 직렬화 문제 해결

리스트를 JSON 배열로 바로 저장하는 경우 Redis 역직렬화에서 타입 정보 문제가 발생할 수 있습니다.

따라서 리스트 응답은 Wrapper DTO로 한 번 감싸 객체 형태로 저장했습니다.

```text
List<DTO>
→ ListResponse<DTO>
```

### 캐시 무효화

캐시를 적용한 조회 데이터는 생성/수정/삭제가 발생했을 때 반드시 무효화해야 합니다.

예를 들어 상품이 카테고리와 연결되어 있는 경우, 상품 변경 시 상품 캐시뿐 아니라 카테고리별 상품 조회 캐시도 함께 무효화해야 합니다.

```text
Item 생성/수정/삭제
→ getItem, getAllItem, getItemsByCategory 캐시 무효화

Category 생성/수정/삭제
→ getRootCategory, getChildCategory, getItemsByCategory 캐시 무효화
```

### 캐시 검증

Redis CLI에서 key 생성을 확인했습니다.

```bash
docker exec -it home-socket-redis redis-cli
keys *
```

---

## 7. 동시성 제어와 Lock

주문, 재고, 쿠폰, 리뷰 평점처럼 동시에 수정될 수 있는 기능에는 동시성 제어가 필요합니다.

### 적용한 방식

- 테이블 unique 제약으로 중복 데이터 방지
- Pessimistic Lock으로 동시에 같은 row를 수정하지 못하도록 제어

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

### 적용 대상

| 기능 | 문제 가능성 | 해결 |
|---|---|---|
| 주문 생성 | 동일 주문 중복 생성 | 주문 생성 흐름에서 필요한 데이터 lock |
| 재고 차감 | 재고가 중복 차감 또는 중복 복구 | Item row lock |
| 쿠폰 사용 | 동일 쿠폰 중복 사용 | CouponPublish row lock |
| 장바구니 추가 | 같은 item이 여러 CartItem으로 생성 | unique 제약 + 존재 여부 쿼리 |
| 리뷰 평점 | 동시 수정 시 평균 평점 불일치 | Item 평점 갱신 시 동시성 고려 |

---

## 8. 결제 테이블과 주문 상태 분리

기존에는 주문과 배송 상태만으로 결제 흐름을 표현하기 어려웠습니다.

이를 개선하기 위해 Payment 테이블을 추가하고, 주문/배송/결제 상태를 분리했습니다.

### 주문 상태

```text
PAYMENT_PENDING
PAID
PAYMENT_FAILED
CANCELED
```

### 배송 상태

```text
PAYMENT_PENDING
READY
SHIPPING
COMPLETED
CANCELED
```

### 결제 승인 흐름

```text
주문 생성
→ 주문/배송 상태 PAYMENT_PENDING
→ 결제 승인 API 호출
→ PaymentService.preparePayment()
→ Mock 외부 결제 API 승인
→ PaymentService.completePayment()
→ 성공 시 Order PAID, Delivery READY
→ 실패 시 Order PAYMENT_FAILED, Delivery CANCELED
```

---

## 9. Kafka 이벤트와 알림 비동기화

결제 완료 이후 알림 저장과 실시간 전송을 주문/결제 트랜잭션에서 직접 처리하지 않고 Kafka 이벤트로 분리했습니다.

### 전체 흐름

```text
PaymentService.completePayment()
→ Payment.approve()
→ Order.markPaid()
→ OrderPaidEvent 생성
→ Transaction Commit 이후 Kafka publish
→ OrderPaidEventConsumer 수신
→ Notification 저장
→ WebSocket 실시간 전송
```

### afterCommit 사용 이유

DB 트랜잭션이 commit되기 전에 Kafka 이벤트가 발행되면, Consumer가 아직 commit되지 않은 주문 상태를 기준으로 동작할 수 있습니다.

이를 막기 위해 `TransactionSynchronization`의 `afterCommit` callback을 사용했습니다.

```text
DB commit 성공
→ Kafka event 발행
```

### Kafka message key

Kafka message key는 `orderId`를 사용했습니다.

같은 주문과 관련된 이벤트가 이후에 늘어났을 때, 동일한 orderId를 key로 사용하면 같은 partition으로 들어갈 가능성이 높아져 순서 관리가 쉬워집니다.

```text
OrderPaidEvent(orderId=1)
OrderCancelledEvent(orderId=1)
OrderRefundedEvent(orderId=1)
```

---

## 10. WebSocket/STOMP 알림

Kafka Consumer가 알림을 저장한 뒤, WebSocket/STOMP를 통해 사용자와 관리자에게 실시간 알림을 전달합니다.

### WebSocket destination

| Destination | 설명 |
|---|---|
| `/user/queue/notifications` | 일반 사용자 알림 |
| `/user/queue/admin-notifications` | 관리자 알림 |

### 인증 방식

REST API는 매 요청마다 HTTP Authorization header를 사용하지만, WebSocket은 다음 흐름을 따릅니다.

```text
HTTP handshake
→ WebSocket 연결
→ STOMP CONNECT frame
→ CONNECT header에 JWT 포함
→ WebSocketAuthChannelInterceptor에서 인증
```

### WebSocketAuthChannelInterceptor 역할

```text
1. STOMP CONNECT 메시지 가로채기
2. Authorization header에서 JWT 추출
3. 만료/위조/tokenVersion 검사
4. DB에서 User 조회
5. Authentication 객체 생성
6. WebSocket session에 사용자 등록
7. SUBSCRIBE 시 관리자 destination은 ROLE_ADMIN만 허용
```

---

## 정리

Home-Socket의 최적화는 단순히 특정 API의 응답 시간을 줄이는 작업이 아니라, 조회 구조, 캐시, 인덱스, 동시성, 결제 상태, 비동기 이벤트, 실시간 알림까지 포함한 구조 개선 작업이었습니다.

핵심은 다음과 같습니다.

```text
Entity 조회 남용 줄이기
→ DTO Projection / EntityGraph / Fetch Join 구분

반복 조회 줄이기
→ Redis Cache

DB 병목 줄이기
→ Index + 실행 계획 확인

동시성 오류 방지
→ Pessimistic Lock + Unique 제약

결제 후속 처리 분리
→ Kafka + afterCommit + WebSocket
```
