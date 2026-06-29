# Home-Socket 트러블슈팅

Home-Socket을 구현하고 배포하면서 겪은 문제를 기능 구현, 성능 개선, 보안, 배포 관점으로 나누어 정리합니다.

---

## 1. JWT 토큰 버전과 기존 토큰 무효화

### 문제

사용자가 정보를 수정하거나 삭제한 이후에도 기존에 발급된 JWT가 계속 사용 가능한 문제가 있었습니다.

JWT는 서버에 상태를 저장하지 않는 stateless token이기 때문에, 한 번 발급된 access token은 만료 전까지 계속 유효합니다. 따라서 계정 정보가 변경되거나 탈퇴된 이후에도 기존 토큰이 인증에 성공할 수 있습니다.

### 원인

토큰 자체에는 발급 당시의 사용자 정보만 들어 있고, DB의 최신 사용자 상태와 토큰의 유효성을 비교하는 기준이 부족했습니다.

### 해결

User에 `tokenVersion` 필드를 추가했습니다.

```text
JWT claims에 tokenVersion 포함
→ 요청 시 DB의 User.tokenVersion과 비교
→ 값이 다르면 기존 토큰 무효 처리
```

사용자 정보 수정, 비밀번호 변경, 탈퇴 등 기존 토큰을 더 이상 허용하면 안 되는 경우 tokenVersion을 증가시키도록 처리했습니다.

### 정리

```text
문제: 사용자 상태 변경 후에도 기존 JWT 사용 가능
해결: tokenVersion 도입
효과: 계정 변경 이후 기존 토큰 무효화 가능
```

---

## 2. 인증 과정에서 User 조회 쿼리 중복 발생

### 문제

인증이 필요한 API 요청에서 User 조회가 여러 번 발생했습니다.

```text
JwtAuthFilter에서 loadUserByUsername()
tokenVersion 확인용 User 조회
Service 계층에서 예외 처리를 위한 User 조회
```

이로 인해 하나의 요청에서 User 관련 쿼리가 3번까지 발생했습니다.

### 해결

`loadUserByUsername()`으로 가져온 UserDetails에 포함된 User 정보를 활용하여 tokenVersion도 함께 확인하도록 수정했습니다.

```text
기존: User 조회 3회
수정: User 조회 2회
```

단, 수정/삭제처럼 영속 상태의 User가 필요한 기능에서는 Service 계층에서 userId 또는 username으로 User를 다시 조회하도록 유지했습니다.

### 배운 점

인증 필터에서 이미 검증한 정보를 Service에서 무조건 다시 조회하면 중복 쿼리가 발생합니다. 반대로 수정/삭제처럼 Dirty Checking이 필요한 경우에는 비영속 User 객체를 그대로 사용하면 안 됩니다.

---

## 3. User 조회 시 Cart 쿼리 발생

### 문제

User 조회 시 Cart 정보까지 함께 조회되는 문제가 있었습니다.

### 원인

User와 Cart의 양방향 연관관계에서 lazy loading 설정이 누락되어 있었습니다.

### 해결

User 엔티티의 Cart 연관관계에 `FetchType.LAZY`를 적용했습니다.

```java
@OneToOne(fetch = FetchType.LAZY)
private Cart cart;
```

### 남은 한계

Cart와 User가 양방향으로 연결되어 있기 때문에, 존재 여부 확인용 쿼리는 일부 남을 수 있습니다.

이를 완전히 제거하려면 인증 객체를 Entity가 아닌 별도의 Custom UserDetails/DTO 형태로 분리하는 것이 더 적합합니다.

---

## 4. `AuthenticationPrincipal`로 받은 User가 비영속 상태인 문제

### 문제

Controller에서 `@AuthenticationPrincipal`로 받은 User 객체를 Service에서 그대로 사용했을 때, 수정/삭제가 정상 반영되지 않거나 연관관계 검증이 예상대로 동작하지 않는 문제가 있었습니다.

### 원인

JWT 인증 과정에서 만들어진 User 객체는 JPA 영속성 컨텍스트가 관리하는 Entity가 아닐 수 있습니다.

```text
AuthenticationPrincipal User
→ 인증 객체로는 사용 가능
→ JPA Dirty Checking 대상은 아닐 수 있음
```

### 해결

수정, 삭제, 소유자 검증이 필요한 기능에서는 userId 또는 username으로 DB에서 User를 다시 조회했습니다.

```java
User user = userRepository.findById(userId)
        .orElseThrow(...);
```

### 정리

- 단순 인증 여부 확인: AuthenticationPrincipal 사용 가능
- 수정/삭제/연관관계 검증: DB에서 영속 User 재조회

---

## 5. SecurityConfig와 필터 처리

### 문제 1. `permitAll()`을 적용했는데도 Filter를 거치는 문제

`permitAll()`은 인증 없이 접근을 허용하지만, Spring Security filter chain 자체를 지나지 않는다는 의미는 아닙니다.

### 해결

JWT 인증이 필요 없는 경로는 JwtAuthFilter의 `shouldNotFilter()`에서 제외했습니다.

```text
permitAll()
→ 인증 없이 접근 허용

shouldNotFilter()
→ 해당 custom filter를 아예 실행하지 않음
```

### 문제 2. 401/403 예외 응답이 공통 JSON 형식으로 내려오지 않는 문제

Security Filter 또는 AuthenticationEntryPoint에서 발생한 예외는 ControllerAdvice가 처리하지 못합니다.

### 해결

SecurityConfig에서 직접 401/403 응답을 JSON으로 작성했습니다.

```text
AuthenticationEntryPoint → 401 JSON 응답
AccessDeniedHandler → 403 JSON 응답
```

### 문제 3. OPTIONS Preflight 요청 차단

CORS 환경에서 실제 요청 전에 OPTIONS preflight 요청이 발생합니다. 이를 허용하지 않으면 실제 API 요청 전에 SecurityConfig에서 차단됩니다.

### 해결

OPTIONS 요청을 허용하고, CORS 설정을 명시했습니다.

---

## 6. CORS 와일드카드 설정 문제

### 문제

개발 환경과 운영 환경에서 frontend origin이 달라 CORS 설정이 꼬이는 문제가 있었습니다.

### 원인

`setAllowedOriginPatterns()`와 `addAllowedOriginPattern()`의 사용 방식 차이를 명확히 이해하지 못하면 wildcard origin 적용이 제대로 되지 않습니다.

### 해결

환경별로 허용 origin을 명확히 분리하고, wildcard pattern이 필요한 경우 `allowedOriginPatterns`를 사용했습니다.

```text
allowedOrigins
→ 명시적인 origin 목록

allowedOriginPatterns
→ wildcard pattern 허용
```

---

## 7. 공통 응답과 예외 포맷

### 문제

프론트엔드와 협업 시 API 요청이 실패했을 때 어떤 이유로 실패했는지 일관되게 확인하기 어려웠습니다.

### 해결

성공/실패/예외 응답을 공통 포맷으로 통일했습니다.

```json
{
  "success": false,
  "message": "에러 메시지",
  "data": null,
  "error": "ERROR_CODE"
}
```

### 추가 처리

다음 예외를 공통 응답으로 정리했습니다.

| 상태 | 예시 |
|---|---|
| 400 | validation 실패 |
| 401 | 인증 실패 |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 500 | 서버 내부 오류 |
| 502/503 | 운영 환경 upstream 오류 |

Security Filter에서 발생하는 예외는 ControllerAdvice가 아닌 SecurityConfig에서 직접 처리했습니다.

---

## 8. 테스트 코드에서 인증 사용자 주입 문제

### 문제

`@AuthenticationPrincipal`로 User를 주입받는 Controller 테스트에서 인증 사용자가 없어 테스트가 실패했습니다.

### 해결

Spring Security Test의 `@WithMockUser` 또는 MockMvc 요청의 `.with(csrf())`를 사용했습니다.

```java
@WithMockUser(username = "user", roles = "USER")
```

```java
mockMvc.perform(post("/api")
        .with(csrf()))
```

### 정리

Swagger/Postman으로 수동 테스트만 하면 반복 검증이 어렵고, 인증·권한·예외 케이스를 놓치기 쉽습니다. 인증 기능은 단위/통합 테스트를 함께 작성하는 것이 필요합니다.

---

## 9. Builder 사용 시 연관관계 List 초기화 문제

### 문제

Entity 필드에서 `new ArrayList<>()`로 초기화했더라도, Lombok Builder를 사용해 객체를 생성하면 해당 초기화가 적용되지 않는 문제가 있었습니다.

### 원인

Lombok `@Builder`는 필드 초기값을 기본적으로 무시할 수 있습니다.

### 해결

연관관계 List에는 `@Builder.Default`를 적용했습니다.

```java
@Builder.Default
@OneToMany(mappedBy = "cart")
private List<CartItem> cartItems = new ArrayList<>();
```

또는 null 허용이 필요한 경우 생성 로직에서 명시적으로 처리했습니다.

---

## 10. DTO Validation과 DB 제약의 역할 분리

### 문제

DTO에 `@NotBlank`를 적용하지 않으면 필수 입력값이 비어 있어도 Controller 단에서 걸러지지 않을 수 있습니다.

반대로 DTO validation만 믿으면 DB에는 잘못된 값이 들어갈 가능성이 남습니다.

### 해결

- 요청 DTO: `@NotBlank`, `@NotNull` 등으로 사용자 입력 검증
- Entity/DDL: `NOT NULL` 제약으로 DB 수준에서 최종 방어

```text
DTO validation
→ API 입력값 검증

DB constraint
→ 데이터 정합성 최종 보장
```

---

## 11. Redis 캐시 직렬화 문제

### 문제

Page 객체나 List 객체를 그대로 Redis에 저장할 때 직렬화/역직렬화 문제가 발생할 수 있습니다.

### 원인

JPA Page 구현체나 raw List는 Redis JSON 직렬화 과정에서 타입 정보가 불명확해질 수 있습니다.

### 해결

Page는 별도의 `PageResponse` DTO로 변환하고, List는 Wrapper DTO로 감싸 객체 형태로 저장했습니다.

```text
Page<Entity>
→ PageResponse<DTO>
→ Redis 저장

List<DTO>
→ ListResponse<DTO>
→ Redis 저장
```

---

## 12. Redis 캐시 무효화 누락 위험

### 문제

캐시를 적용한 조회 API의 원본 데이터가 생성/수정/삭제되었는데, 캐시를 무효화하지 않으면 오래된 데이터가 계속 반환될 수 있습니다.

### 해결

데이터 변경 API마다 관련 캐시를 함께 무효화했습니다.

```text
Item 변경
→ 상품 단건/목록/카테고리별 상품 캐시 무효화

Category 변경
→ 루트/자식 카테고리/카테고리별 상품 캐시 무효화

Review 변경
→ 리뷰/상품 상세 관련 캐시 무효화
```

### 배운 점

캐시는 조회 성능을 개선하지만, 변경 지점에서 무효화 전략을 함께 설계하지 않으면 데이터 정합성을 해칠 수 있습니다.

---

## 13. MySQL 기반 마이그레이션과 인덱스 관리의 한계

### 문제

인덱스 적용과 삭제를 migration으로 반복하면서 MySQL에서는 조건부 index 생성/삭제가 불편했습니다.

특히 인덱스 생성 중 오류가 발생하면 실제 인덱스는 생성되지 않았지만, migration 로그 때문에 이후 실행이 꼬이는 문제가 생길 수 있었습니다.

### 해결

PostgreSQL로 전환하고, 인덱스 생성에는 `IF NOT EXISTS`를 사용했습니다.

```sql
CREATE INDEX IF NOT EXISTS idx_item_category_id ON item(category_id);
```

### 정리

```text
MySQL
→ 일부 migration 제어가 복잡

PostgreSQL
→ IF NOT EXISTS 기반 index migration 관리가 비교적 단순
```

---

## 14. Lock과 Unique 제약으로 동시성 문제 방지

### 문제

동시에 같은 기능이 실행되면 다음 문제가 발생할 수 있었습니다.

- 주문이 중복 생성됨
- 재고가 두 번 차감됨
- 주문 취소 시 재고가 두 번 복구됨
- 쿠폰이 중복 사용됨
- 같은 장바구니에 같은 상품이 여러 줄로 추가됨
- 리뷰 평점이 잘못 계산됨

### 해결

동시에 수정될 수 있는 row에는 비관적 락을 적용했습니다.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

중복 데이터 자체를 DB 수준에서 막아야 하는 경우에는 unique 제약을 사용했습니다.

```text
cart_id + item_id unique
user_id + coupon_id unique
```

---

## 15. 결제 상태와 배송 상태 분리

### 문제

주문과 배송 상태만으로 결제 성공/실패/대기 상태를 명확히 표현하기 어려웠습니다.

### 해결

Payment 테이블을 추가하고, Order/Delivery/Payment 상태를 분리했습니다.

```text
Order: PAYMENT_PENDING, PAID, PAYMENT_FAILED, CANCELED
Delivery: PAYMENT_PENDING, READY, SHIPPING, COMPLETED, CANCELED
Payment: READY, APPROVED, FAILED
```

결제 승인 흐름은 다음과 같이 정리했습니다.

```text
주문 생성
→ 주문/배송 PAYMENT_PENDING
→ 결제 승인 API 호출
→ 금액 검증
→ Mock 결제 승인
→ 성공 시 Order PAID, Delivery READY
→ 실패 시 Order PAYMENT_FAILED, Delivery CANCELED
```

---

## 16. Kafka 이벤트 발행 시점 문제

### 문제

결제 완료 이벤트를 DB commit 전에 Kafka로 발행하면, Consumer가 아직 commit되지 않은 주문/결제 상태를 기준으로 동작할 수 있습니다.

### 해결

`TransactionSynchronization`의 `afterCommit` callback을 사용해 DB commit 이후 Kafka 이벤트를 발행했습니다.

```text
PaymentService.completePayment()
→ DB transaction commit
→ afterCommit()
→ KafkaTemplate.send(order.paid)
```

### 배운 점

외부 메시지 시스템으로 이벤트를 발행할 때는 DB 트랜잭션 성공 여부와 발행 시점을 함께 고려해야 합니다.

---

## 17. Kafka message key와 순서 보장

### 문제

같은 주문에 대한 이벤트가 여러 종류로 확장될 경우 서로 다른 partition에 들어가면 순서 관리가 어려워질 수 있습니다.

예상 가능한 이벤트:

```text
OrderPaidEvent(orderId=1)
OrderCancelledEvent(orderId=1)
OrderRefundedEvent(orderId=1)
```

### 해결

Kafka message key로 `orderId`를 사용했습니다.

```text
key = orderId
```

같은 key를 가진 메시지는 같은 partition으로 들어가므로, 같은 주문 단위의 이벤트 순서를 관리하기 쉬워집니다.

---

## 18. WebSocket/STOMP 인증 처리

### 문제

REST API는 매 요청마다 Authorization header에 JWT를 담지만, WebSocket은 최초 HTTP handshake 이후 STOMP frame으로 통신합니다.

따라서 기존 HTTP JWT filter만으로는 WebSocket 메시지 인증을 처리할 수 없습니다.

### 해결

WebSocket handshake 경로는 SecurityConfig에서 허용하고, STOMP CONNECT frame에서 JWT를 검사했습니다.

```text
HTTP handshake
→ STOMP CONNECT
→ Authorization header에서 JWT 추출
→ WebSocketAuthChannelInterceptor에서 인증
```

### Interceptor 역할

- CONNECT frame에서 JWT 추출
- tokenVersion 검사
- User 조회
- Authentication 생성
- WebSocket session에 사용자 등록
- 관리자 알림 destination 구독 시 ROLE_ADMIN 검증

---

## 19. Nginx 502 Bad Gateway

### 문제

OAuth 로그인 또는 API 요청 시 Nginx에서 502가 발생했습니다.

### 원인

Spring Boot app 컨테이너가 아직 완전히 기동되지 않은 상태에서 Nginx가 upstream으로 요청을 전달했습니다.

### 해결

app 로그와 컨테이너 상태를 확인하고, CI/CD health check에 충분한 retry 시간을 추가했습니다.

```bash
docker compose -f docker-compose.prod.yml logs --tail=300 app
curl -i http://127.0.0.1:8081/v3/api-docs
```

---

## 20. Spring Boot 직접 외부 노출 방지

### 문제

Spring Boot app을 `8081:8081`로 열 경우 외부에서 Nginx를 거치지 않고 직접 접근할 수 있습니다.

### 해결

Docker Compose에서 app 포트를 host loopback에만 바인딩했습니다.

```yaml
ports:
  - "127.0.0.1:8081:8081"
```

이 구성으로 외부 사용자는 Nginx를 통해서만 요청할 수 있습니다.

---

## 21. Kafka hostname과 log directory 문제

### 문제

Spring Boot에서 Kafka hostname을 찾지 못하거나 Kafka 컨테이너가 log directory에 파일을 쓰지 못하는 문제가 발생했습니다.

### 해결

Spring Boot app과 Kafka를 같은 Docker Compose network에 두고, app에서는 Compose service name인 `kafka`를 사용했습니다.

```properties
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092
```

Kafka 데이터 디렉터리는 host bind mount로 구성하고 권한을 조정했습니다.

```bash
mkdir -p kafka-data
sudo chown -R 1000:1000 kafka-data
sudo chmod -R 775 kafka-data
```

---

## 22. GitHub Actions secret 누락과 Health Check 시간 부족

### 문제

GitHub Actions에서 SSH host 또는 private key 값이 비어 deploy 단계가 실패했습니다. 또한 서버 사양이 낮아 Spring Boot 기동 시간이 길어지면서 health check가 너무 빨리 실패했습니다.

### 해결

배포 workflow에 secret 검증 단계를 추가했습니다.

```text
OCI_HOST
OCI_USER
OCI_SSH_KEY
APP_DIR
```

그리고 health check를 단발성이 아니라 retry loop로 변경했습니다.

```bash
for i in {1..60}; do
  if curl --max-time 5 -f -s -o /dev/null http://127.0.0.1:8081/v3/api-docs; then
    echo "Health check success"
    break
  fi
  sleep 5
done
```

---

## 23. 운영 서버 보안 설정

### 문제

운영 도메인을 외부에 공개하면 SSH brute-force, `.env` 탐색, 비정상적인 404/403 요청 등이 발생합니다.

### 해결

- SSH key-only 인증 사용
- PasswordAuthentication 비활성화
- Nginx access log 기반 Fail2Ban jail 구성
- OCI Security List와 iptables로 필요한 포트만 허용

---

## 정리

Home-Socket의 트러블슈팅은 기능 구현 문제와 운영 배포 문제를 함께 다뤘습니다.

```text
인증/인가
→ tokenVersion, Security 예외 처리, WebSocket 인증

성능/데이터
→ DTO Projection, Redis 직렬화, Cache Evict, Index migration

동시성
→ Lock, unique 제약, 결제 상태 분리

이벤트/실시간
→ Kafka afterCommit, message key, STOMP 인증

운영
→ Nginx, Docker Compose, GitHub Actions, Fail2Ban
```
