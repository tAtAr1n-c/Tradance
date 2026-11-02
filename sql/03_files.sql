CREATE TABLE files(
    id bigserial primary key,
    original_name varchar(255) not null,
    storage_name varchar(255) not null unique,
    content_type varchar(100) not null,
    size bigint not null check(size >= 0),
    user_id bigint not null references users(id),
    created_at timestamp not null default now()
);

ALTER TABLE users DROP COLUMN avatar_path;
ALTER TABLE users ADD COLUMN avatar_file_id bigint null references files(id) on delete set null;

ALTER TABLE currencies DROP COLUMN icon_path;
ALTER TABLE currencies ADD COLUMN icon_file_id bigint null references files(id) on delete set null;