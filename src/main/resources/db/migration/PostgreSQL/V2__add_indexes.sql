-- Order
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);

-- User
CREATE INDEX IF NOT EXISTS idx_user_username ON "user"(username);

-- Address
CREATE INDEX IF NOT EXISTS idx_address_user_id ON address(user_id);

-- Coupon
CREATE INDEX IF NOT EXISTS idx_coupon_publish_user_id ON coupon_publish(user_id);
CREATE INDEX IF NOT EXISTS idx_coupon_publish_status ON coupon_publish(status);
CREATE INDEX IF NOT EXISTS idx_coupon_publish_coupon_id ON coupon_publish(coupon_id);

-- Cart
CREATE INDEX IF NOT EXISTS idx_cart_user_id ON cart(user_id);

-- CartItem
CREATE INDEX IF NOT EXISTS idx_cart_item_cart_id ON cart_item(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_item_item_id ON cart_item(item_id);
