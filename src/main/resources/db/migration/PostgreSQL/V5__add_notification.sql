CREATE TABLE notification (
    notification_id BIGSERIAL PRIMARY KEY,
    -- 중복 방지용 (같은 이벤트 알림 생성)
    dedup_key VARCHAR(150) NOT NULL UNIQUE,
    event_id VARCHAR(80) NOT NULL,
    receiver_type VARCHAR(30) NOT NULL,
    receiver_user_id BIGINT,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(500) NOT NULL,
    related_order_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_notification_user
        FOREIGN KEY (receiver_user_id)
        REFERENCES "user"(user_id),

    CONSTRAINT fk_notification_order
        FOREIGN KEY (related_order_id)
        REFERENCES orders(order_id)
);

CREATE INDEX idx_notification_receiver ON notification(receiver_type, receiver_user_id, is_read, created_at DESC);
CREATE INDEX idx_notification_order ON notification(related_order_id);