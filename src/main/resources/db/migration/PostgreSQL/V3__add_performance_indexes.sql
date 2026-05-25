-- Review / Address
CREATE INDEX IF NOT EXISTS idx_address_user_id ON address(user_id);
CREATE INDEX idx_review_user_id_review_id ON review(user_id, review_id DESC);
CREATE INDEX idx_review_item_id_review_id ON review(item_id, review_id DESC);

-- OrderDetail
CREATE INDEX IF NOT EXISTS idx_order_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_delivery_order_id ON delivery(order_id);
CREATE INDEX IF NOT EXISTS idx_order_item_order_id ON order_item(order_id);

-- OrderPage
CREATE INDEX IF NOT EXISTS idx_coupon_publish_user_status ON coupon_publish(user_id, status);
CREATE INDEX IF NOT EXISTS idx_cart_item_cart_id ON cart_item(cart_id);

-- CreateOrder / deleteItems
CREATE INDEX IF NOT EXISTS idx_cart_user_id ON cart(user_id);

