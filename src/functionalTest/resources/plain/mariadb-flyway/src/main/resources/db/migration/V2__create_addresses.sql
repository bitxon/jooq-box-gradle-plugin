CREATE TABLE addresses
(
    id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT          NOT NULL,
    country VARCHAR(100) NOT NULL,
    zip     VARCHAR(20)  NOT NULL,
    city    VARCHAR(100) NOT NULL,
    street  VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);
