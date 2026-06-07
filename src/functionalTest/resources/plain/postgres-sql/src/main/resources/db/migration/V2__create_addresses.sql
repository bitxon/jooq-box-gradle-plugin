CREATE TABLE addresses
(
    id      SERIAL PRIMARY KEY,
    user_id INTEGER      NOT NULL REFERENCES users (id),
    country VARCHAR(100) NOT NULL,
    zip     VARCHAR(20)  NOT NULL,
    city    VARCHAR(100) NOT NULL,
    street  VARCHAR(255) NOT NULL
);
