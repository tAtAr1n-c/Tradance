CREATE TABLE deposits
(
    id          bigserial primary key,
    user_id     bigint         not null references users (id),
    currency_id bigint         not null references currencies (id),
    admin_id    bigint         not null references users (id),
    amount      numeric(30, 8) not null check (amount > 0),
    created_at  timestamp      not null default now()
);

CREATE INDEX deposits_user_idx ON deposits (user_id);
