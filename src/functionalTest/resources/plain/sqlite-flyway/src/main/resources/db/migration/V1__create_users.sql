CREATE TABLE users
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT    NOT NULL,
    points_amount INTEGER NOT NULL DEFAULT 0,
    date_of_birth TEXT    NOT NULL
);
