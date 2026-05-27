create table if not exists in_app_notifications (
    id bigserial primary key,
    user_id bigint not null,
    type varchar(50) not null,
    title varchar(180) not null,
    message varchar(1000) not null,
    order_reference varchar(32),
    unread boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_in_app_notifications_user
        foreign key (user_id) references users(id) on delete cascade
);

create index if not exists idx_in_app_notifications_user_created_at
    on in_app_notifications(user_id, created_at desc);

create table if not exists user_device_tokens (
    id bigserial primary key,
    user_id bigint not null,
    token varchar(512) not null unique,
    platform varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_user_device_tokens_user
        foreign key (user_id) references users(id) on delete cascade
);

create index if not exists idx_user_device_tokens_user_id
    on user_device_tokens(user_id);
