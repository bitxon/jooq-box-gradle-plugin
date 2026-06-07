CREATE TABLE users
(
    id   SERIAL PRIMARY KEY,
    name   VARCHAR(255) NOT NULL,
    points_amount BIGINT NOT NULL DEFAULT 0,
    date_of_birth DATE NOT NULL
);
