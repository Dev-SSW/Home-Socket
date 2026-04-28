CREATE INDEX IF NOT EXISTS idx_order_user_id ON orders(user_id);

CREATE INDEX IF NOT EXISTS idx_cart_user_id ON cart(user_id);

CREATE INDEX IF NOT EXISTS idx_order_item_order_id ON order_item(order_id);

CREATE INDEX IF NOT EXISTS idx_delivery_order_id ON delivery(order_id);