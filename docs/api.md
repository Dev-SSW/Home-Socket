# Home-Socket 주요 API

자세한 request/response는 Swagger UI에서 확인합니다.

```text
http://localhost:8081/swagger-ui/index.html
```

---

## Public API

| Method | Path | 설명 |
|---|---|---|
| `POST` | `/public/signup` | 회원가입 |
| `POST` | `/public/login` | 로그인/JWT 발급 |
| `POST` | `/public/tokenRenew` | access/refresh token 재발급 |
| `POST` | `/public/validateTest` | 토큰 유효성 테스트 |
| `GET` | `/public/category/getRootCategory` | 루트 카테고리 조회 |
| `GET` | `/public/category/getChildrenCategory/{parentId}` | 하위 카테고리 조회 |
| `GET` | `/public/item/getAllItem` | 전체 상품 페이지 조회 |
| `GET` | `/public/item/getItem/{itemId}` | 상품 단건 조회 |
| `GET` | `/public/item/getItemsByCategory/{categoryId}` | 카테고리별 상품 페이지 조회 |
| `POST` | `/public/mock-payments/confirm` | Mock 결제 승인 |

---

## User API

| Method | Path | 설명 |
|---|---|---|
| `GET` | `/user/getUser` | 내 정보 조회 |
| `PUT` | `/user/updateUser` | 내 정보 수정 |
| `PUT` | `/user/updatePassword` | 비밀번호 수정 |
| `DELETE` | `/user/deleteUser` | 회원 탈퇴 |
| `POST` | `/user/address/createAddress` | 배송지 생성 |
| `GET` | `/user/address/getAllAddress` | 내 배송지 목록 조회 |
| `GET` | `/user/address/{addressId}/getAddress` | 배송지 단건 조회 |
| `PUT` | `/user/address/{addressId}/updateAddress` | 배송지 수정 |
| `DELETE` | `/user/address/{addressId}/deleteAddress` | 배송지 삭제 |
| `PUT` | `/user/address/{addressId}/updateDefault` | 기본 배송지 변경 |
| `GET` | `/user/cart/getCart` | 장바구니 조회 |
| `POST` | `/user/cart/addItem` | 장바구니 상품 추가 |
| `PUT` | `/user/cart/updateItem` | 장바구니 상품 수량 변경 |
| `DELETE` | `/user/cart/{cartItemId}/deleteItem` | 장바구니 상품 삭제 |
| `DELETE` | `/user/cart/deleteItems` | 장바구니 선택 삭제 |
| `DELETE` | `/user/cart/clearCart` | 장바구니 비우기 |
| `POST` | `/user/coupon/{couponId}/couponPublish/publishCoupon` | 쿠폰 발급 |
| `GET` | `/user/coupon/couponPublish/getCouponPublish` | 내 발급 쿠폰 조회 |
| `POST` | `/user/order/createCartOrder` | 장바구니 기반 주문 생성 |
| `DELETE` | `/user/order/{orderId}/cancelOrder` | 주문 취소 |
| `GET` | `/user/order/getOrderList` | 내 주문 목록 조회 |
| `GET` | `/user/order/{orderId}/getOrderDetail` | 주문 상세 조회 |
| `GET` | `/user/order/getOrderPage` | 주문 페이지 데이터 조회 |
| `POST` | `/user/payments/confirm` | 결제 승인 |
| `POST` | `/user/item/{itemId}/review/createReview/` | 리뷰 생성 |
| `PUT` | `/user/item/review/{reviewId}/updateReview/` | 리뷰 수정 |
| `DELETE` | `/user/item/review/{reviewId}/deleteReview/` | 리뷰 삭제 |
| `GET` | `/user/item/review/{reviewId}/getReview/` | 리뷰 단건 조회 |
| `GET` | `/user/getUserReview/` | 내 리뷰 조회 |
| `GET` | `/user/item/{itemId}/review/getItemReview/` | 상품 리뷰 조회 |

---

## Admin API

| Method | Path | 설명 |
|---|---|---|
| `GET` | `/admin/getAllUser` | 전체 회원 페이지 조회 |
| `POST` | `/admin/category/createCategory` | 카테고리 생성 |
| `PUT` | `/admin/category/updateCategory/{categoryId}` | 카테고리 수정 |
| `DELETE` | `/admin/category/deleteCategory/{categoryId}` | 카테고리 삭제 |
| `POST` | `/admin/category/{categoryId}/item/createItem/` | 상품 생성 |
| `PUT` | `/admin/item/updateItem/{itemId}` | 상품 수정 |
| `DELETE` | `/admin/item/deleteItem/{itemId}` | 상품 삭제 |
| `POST` | `/admin/coupon/createCoupon` | 쿠폰 생성 |
| `GET` | `/admin/coupon/getAllCoupon` | 쿠폰 목록 조회 |
| `GET` | `/admin/coupon/{couponId}/getCoupon` | 쿠폰 단건 조회 |
| `PUT` | `/admin/coupon/{couponId}/updateCoupon` | 쿠폰 수정 |
| `DELETE` | `/admin/coupon/{couponId}/deleteCoupon` | 쿠폰 삭제 |
| `PUT` | `/admin/delivery/{deliveryId}/updateDeliveryStatus` | 배송 상태 변경 |

---

## WebSocket

STOMP endpoint:

```text
ws://localhost:8081/ws
http://localhost:8081/ws-sockjs
```

CONNECT header:

```text
Authorization: Bearer <access-token>
```

| Destination | 설명 |
|---|---|
| `/user/queue/notifications` | 사용자 개인 알림 |
| `/user/queue/admin-notifications` | 관리자 알림 |
