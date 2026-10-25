CREATE TABLE users(
                      id bigserial primary key,
                      username varchar(30) not null unique,
                      email varchar(255) not null unique,
                      password_hash varchar(100) not null,
                      role varchar(10) not null default('USER') check(role in('USER', 'ADMIN')),
                      is_blocked boolean not null default false,
                      avatar_path varchar(255) null,
                      created_at timestamp not null default now()
);


CREATE TABLE recovery_words(
    id bigserial primary key,
    user_id bigint not null references users(id) on delete cascade,
    position smallint not null check(position between 1 and 10),
    word_hash varchar(100) not null,
    unique(user_id, position)
);


CREATE TABLE currencies(
    id bigserial primary key,
    ticker varchar(10) not null unique,
    name varchar(50) not null,
    icon_path varchar(255) null,
    is_quote boolean not null default false,
    is_active boolean not null default true
);

CREATE TABLE wallets(
    id bigserial primary key,
    user_id bigint not null references users(id),
    currency_id bigint not null references currencies(id),
    balance numeric(30, 8) not null default 0 check(balance >= 0),
    locked numeric(30, 8) not null default 0 check(locked >= 0 and locked <= balance),
    unique(user_id, currency_id)
);



CREATE TABLE orders(
    id bigserial primary key,
    user_id bigint references users(id) not null,
    currency_id bigint not null references currencies(id),
    side varchar(4) not null check(side in('BUY', 'SELL')),
    price numeric(30, 8) not null check(price > 0),
    amount numeric(30, 8) not null check(amount > 0 ),
    filled numeric(30, 8) not null default 0 check(0 <= filled and filled <= amount),
    status varchar(10) not null check(status in('OPEN', 'PARTIAL', 'FILLED', 'CANCELLED')),
    created_at timestamp not null default now()
);

CREATE TABLE trades(
    id bigserial primary key,
    buy_order_id bigint not null references orders(id),
    sell_order_id bigint not null references orders(id),
    buyer_id bigint not null references users(id),
    seller_id bigint not null references users(id),
    currency_id bigint not null references currencies(id),
    price numeric(30, 8) not null,
    amount numeric(30, 8) not null,
    created_at timestamp not null default now()
);

CREATE TABLE favorites(
    user_id bigint references users(id) on delete cascade,
    currency_id bigint references currencies(id) on delete cascade,
    primary key(user_id, currency_id)
);



