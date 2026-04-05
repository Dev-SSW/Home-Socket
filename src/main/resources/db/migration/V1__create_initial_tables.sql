CREATE TABLE user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    birth DATE NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    token_version INT NOT NULL DEFAULT 1
);

CREATE TABLE category (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    depth INT NOT NULL,
    order_index INT NOT NULL,
    parent_id BIGINT,
    FOREIGN KEY (parent_id) REFERENCES category(category_id)
);

CREATE TABLE item (
    item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    stock INT NOT NULL,
    item_price INT NOT NULL,
    avg_star FLOAT DEFAULT 0,
    category_id BIGINT NOT NULL,
    FOREIGN KEY (category_id) REFERENCES category(category_id)
);

CREATE TABLE coupon (
    coupon_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    discount DECIMAL(10,2) NOT NULL,
    valid_start DATE NOT NULL,
    valid_end DATE NOT NULL,
    after_issue INT NOT NULL
);

CREATE TABLE coupon_publish (
    coupon_publish_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    valid_start DATE NOT NULL,
    valid_end DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (coupon_id) REFERENCES coupon(coupon_id)
);

CREATE TABLE orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_date DATE,
    total_price DECIMAL(10,2) NOT NULL,
    coupon_publish_id BIGINT,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (coupon_publish_id) REFERENCES coupon_publish(coupon_publish_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

CREATE TABLE order_item (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantity INT NOT NULL,
    order_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (item_id) REFERENCES item(item_id)
);

CREATE TABLE address (
    Address_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    street VARCHAR(255) NOT NULL,
    detail_street VARCHAR(255) NOT NULL,
    zipcode VARCHAR(20) NOT NULL,
    default_address BOOLEAN DEFAULT FALSE,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

CREATE TABLE delivery (
    delivery_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    order_id BIGINT NOT NULL,
    address_id BIGINT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (address_id) REFERENCES address(address_id)
);

CREATE TABLE cart (
    cart_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

CREATE TABLE cart_item (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantity INT NOT NULL,
    cart_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES cart(cart_id),
    FOREIGN KEY (item_id) REFERENCES item(item_id)
);

CREATE TABLE review (
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    comment TEXT,
    star FLOAT NOT NULL,
    review_date DATE,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (item_id) REFERENCES item(item_id)
);