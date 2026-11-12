CREATE TABLE instruments (
    symbol varchar(10) PRIMARY KEY,
    name varchar(80) NOT NULL
);

CREATE TABLE quotes (
    symbol varchar(10) PRIMARY KEY REFERENCES instruments (symbol),
    price numeric(19, 4) NOT NULL CHECK (price > 0),
    as_of timestamptz NOT NULL
);

CREATE TABLE users (
    id uuid PRIMARY KEY,
    email varchar(255) NOT NULL UNIQUE,
    password_hash varchar(255) NOT NULL,
    created_at timestamptz NOT NULL
);

CREATE TABLE accounts (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL UNIQUE REFERENCES users (id),
    cash_balance numeric(19, 2) NOT NULL CHECK (cash_balance >= 0)
);

CREATE TABLE orders (
    id uuid PRIMARY KEY,
    account_id uuid NOT NULL REFERENCES accounts (id),
    symbol varchar(10) NOT NULL REFERENCES instruments (symbol),
    side varchar(4) NOT NULL CHECK (side IN ('BUY', 'SELL')),
    quantity integer NOT NULL CHECK (quantity > 0),
    status varchar(16) NOT NULL,
    fill_price numeric(19, 4) NOT NULL CHECK (fill_price > 0),
    realized_pnl numeric(19, 2),
    created_at timestamptz NOT NULL
);

CREATE INDEX orders_account_id_idx ON orders (account_id, created_at DESC);

CREATE TABLE positions (
    id uuid PRIMARY KEY,
    account_id uuid NOT NULL REFERENCES accounts (id),
    symbol varchar(10) NOT NULL REFERENCES instruments (symbol),
    quantity integer NOT NULL CHECK (quantity > 0),
    average_cost numeric(19, 4) NOT NULL CHECK (average_cost > 0),
    UNIQUE (account_id, symbol)
);

INSERT INTO instruments (symbol, name) VALUES
    ('AAPL', 'Apple'),
    ('MSFT', 'Microsoft'),
    ('GOOGL', 'Alphabet'),
    ('AMZN', 'Amazon'),
    ('NVDA', 'NVIDIA'),
    ('META', 'Meta'),
    ('TSLA', 'Tesla'),
    ('JPM', 'JPMorgan Chase'),
    ('V', 'Visa'),
    ('DIS', 'Disney');

INSERT INTO quotes (symbol, price, as_of) VALUES
    ('AAPL', 190.0000, now()),
    ('MSFT', 420.0000, now()),
    ('GOOGL', 175.0000, now()),
    ('AMZN', 200.0000, now()),
    ('NVDA', 140.0000, now()),
    ('META', 580.0000, now()),
    ('TSLA', 250.0000, now()),
    ('JPM', 220.0000, now()),
    ('V', 310.0000, now()),
    ('DIS', 100.0000, now());
