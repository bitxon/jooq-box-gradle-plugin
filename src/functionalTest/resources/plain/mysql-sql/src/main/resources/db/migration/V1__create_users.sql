CREATE TABLE users
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name   VARCHAR(255) NOT NULL,
    points_amount BIGINT NOT NULL DEFAULT 0,
    date_of_birth DATE NOT NULL
);
