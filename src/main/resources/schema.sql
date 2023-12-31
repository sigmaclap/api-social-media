drop table if exists users cascade;
drop table if exists posts cascade;
drop table if exists friendship cascade;


create table if not exists users (
    id bigint generated by default as identity not null,
    username text not null,
    email text not null,
    password text,
    constraint pk_users primary key (id),
    constraint uq_users_email unique (email)
);

create table if not exists posts (
    id bigint generated by default as identity not null,
    description varchar(255) not null,
    text varchar(255) not null,
    image varchar(255) not null,
    owner_id BIGINT not null REFERENCES USERS(ID) ON delete CASCADE,
    created_date timestamp without time zone not null,
    constraint pk_posts primary key (id)
);

create table if not exists friendship (
    id bigint generated by default as identity not null,
    follower_id bigint references users(id),
    friend_id bigint references users(id),
    state varchar not null,
    chat varchar not null
);