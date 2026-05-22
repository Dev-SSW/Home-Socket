-- 쿠폰 중복 발급 방지 / 장바구니 아이템 중복 추가 방지
CREATE UNIQUE INDEX uk_coupon_publish_user_coupon ON coupon_publish(user_id, coupon_id);
CREATE UNIQUE INDEX uk_cart_item_cart_item ON cart_item(cart_id, item_id);