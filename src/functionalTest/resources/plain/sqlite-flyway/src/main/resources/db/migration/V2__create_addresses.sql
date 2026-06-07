CREATE TABLE addresses
(
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users (id),
    country TEXT    NOT NULL,
    zip     TEXT    NOT NULL,
    city    TEXT    NOT NULL,
    street  TEXT    NOT NULL
);
