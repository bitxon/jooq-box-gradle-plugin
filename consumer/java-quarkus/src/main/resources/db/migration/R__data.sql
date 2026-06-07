INSERT INTO account (id, email, first_name, last_name, date_of_birth, currency, money_amount)
VALUES (1, 'alice@mail.com', 'Alice', 'Anderson', '1991-01-21', 'USD', 340),
       (2, 'bob@mail.com', 'Bob', 'Brown', '1992-02-22', 'USD', 573),
       (3, 'conor@mail.com', 'Conor', 'Carter', '1993-03-23', 'USD', 79),
       (4, 'dilan@mail.com', 'Dilan', 'Davis', '1994-04-24', 'USD', 33),
       (5, 'eva@mail.com', 'Eva', 'Edwards', '1995-05-25', 'USD', 100),
       (6, 'frank@mail.com', 'Frank', 'Fox', '1996-06-26', 'GBP', 80)
ON CONFLICT (id)
    DO UPDATE SET (email, first_name, last_name, date_of_birth, currency, money_amount) =
                      (excluded.email, excluded.first_name, excluded.last_name, excluded.date_of_birth, excluded.currency, excluded.money_amount);
