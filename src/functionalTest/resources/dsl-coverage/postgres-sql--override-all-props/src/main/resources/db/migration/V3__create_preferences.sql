CREATE TABLE preferences
(
    id      SERIAL PRIMARY KEY,
    user_id INTEGER      NOT NULL REFERENCES users (id),
    key     VARCHAR(255) NOT NULL,
    value   VARCHAR(255) NOT NULL,
    active  BOOLEAN      NOT NULL DEFAULT TRUE
);
