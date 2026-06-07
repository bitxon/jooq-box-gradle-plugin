CREATE TABLE account (
    id SERIAL PRIMARY KEY,
    email VARCHAR ( 50 ) NOT NULL,
    first_name VARCHAR ( 50 ) NOT NULL,
    last_name VARCHAR ( 50 ) NOT NULL,
    date_of_birth DATE NOT NULL,
    currency VARCHAR ( 50 ) NOT NULL,
    money_amount INT NOT NULL
);
