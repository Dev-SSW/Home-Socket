CREATE INDEX IF NOT EXISTS idx_address_user_id ON address(user_id);

CREATE INDEX IF NOT EXISTS idx_order_user_id ON orders(user_id);

CREATE INDEX IF NOT EXISTS idx_coupon_publish_user_status ON coupon_publish(user_id, status);

CREATE INDEX IF NOT EXISTS idx_cart_item_cart_id ON cart_item(cart_id);

CREATE INDEX IF NOT EXISTS idx_user_username ON "user"(username);

CREATE INDEX IF NOT EXISTS idx_item_category_id ON item(category_id);

CREATE INDEX IF NOT EXISTS idx_category_parent_id ON category(parent_id);
