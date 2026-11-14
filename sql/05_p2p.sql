create table payment_methods(
    id   bigserial primary key,
    name varchar(100) not null unique
);

create table p2p_offers(
    id bigserial primary key,
    user_id bigint not null references users(id),
    currency_id bigint not null references currencies(id),
    payment_method_id bigint not null references payment_methods(id),
    price numeric(30, 2) not null check(price > 0),
    amount numeric(30, 8) not null check(amount > 0),
    remaining numeric(30, 8) not null check(remaining >= 0 and remaining <= amount),
    status varchar(10) not null check(status in ('ACTIVE', 'CLOSED')),
    created_at timestamp not null default now()
);

create table p2p_deals(
    id bigserial primary key,
    offer_id bigint not null references p2p_offers(id),
    buyer_id bigint not null references users(id),
    cancelled_by bigint references users(id),
    amount numeric(30, 8) not null check(amount > 0),
    status varchar(10) not null check(status in ('CREATED', 'PAID', 'COMPLETED', 'CANCELLED')),
    created_at timestamp not null default now(),
    paid_at timestamp,
    closed_at timestamp
);





insert into payment_methods (name) values
                                       ('СБП'),
                                       ('Т-Банк'),
                                       ('Сбербанк');