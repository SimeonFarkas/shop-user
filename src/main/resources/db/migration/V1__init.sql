CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL
);

CREATE TABLE customer_order (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                user_email VARCHAR(100) NOT NULL,
                                created_at DATETIME NOT NULL,
                                status VARCHAR(20) NOT NULL
);

CREATE TABLE order_item (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            product_id BIGINT NOT NULL,
                            product_title VARCHAR(255) NOT NULL,
                            price DOUBLE NOT NULL,
                            quantity INT NOT NULL,
                            order_id BIGINT,
                            FOREIGN KEY (order_id) REFERENCES customer_order(id)
);