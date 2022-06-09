begin transaction;

create table users
(
    user_id    varchar primary key,
    created_on timestamp default now()
);

create type profile_type as enum (
    'PERSONAL',
    'BUSINESS'
    );

create table profiles
(
    profile_id uuid primary key,
    alias      varchar,
    type       profile_type,
    user_id    varchar,
    created_on timestamp default now(),
    updated_on timestamp default now(),
    foreign key (user_id) references users (user_id) on delete restrict on update restrict
);

create table users_profiles
(
    user_id    varchar,
    profile_id uuid,
    foreign key (user_id) references users (user_id) on delete restrict on update restrict,
    foreign key (profile_id) references profiles (profile_id) on delete restrict on update restrict
);

create type currency as enum (
    'CHF',
    'DKK',
    'EUR',
    'GBP',
    'NOK',
    'SEK',
    'USD'
    );

create table fiat_custody_accounts
(
    fiat_custody_account_id uuid primary key,
    balance                 bigint,
    reserved_balance        bigint,
    currency                currency,
    alias                   varchar,
    created_on              timestamp default now(),
    updated_on              timestamp default now()
);

create table fiat_stakeholder_accounts
(
    fiat_stakeholder_account_id uuid primary key,
    balance                     bigint,
    reserved_balance            bigint,
    currency                    currency,
    alias                       varchar,
    fiat_custody_account_id     uuid,
    profile_id                  uuid,
    created_on                  timestamp default now(),
    updated_on                  timestamp default now(),
    foreign key (profile_id) references profiles (profile_id) on delete restrict on update restrict,
    foreign key (fiat_custody_account_id) references fiat_custody_accounts (fiat_custody_account_id) on delete restrict on update restrict
);


create table portfolios
(
    portfolio_id                uuid primary key,
    alias                       varchar,
    fiat_stakeholder_account_id uuid,
    created_on                  timestamp default now(),
    updated_on                  timestamp default now(),
    foreign key (fiat_stakeholder_account_id) references fiat_stakeholder_accounts (fiat_stakeholder_account_id) on delete restrict on update restrict
);

create domain crypto_currency as varchar;

create table crypto_custody_accounts
(
    crypto_custody_account_id uuid primary key,
    balance                   bigint,
    reserved_balance          bigint,
    crypto_currency           crypto_currency,
    alias                     varchar,
    created_on                timestamp default now(),
    updated_on                timestamp default now()
);

create table crypto_stakeholder_accounts
(
    crypto_stakeholder_account_id uuid primary key,
    balance                       bigint,
    reserved_balance              bigint,
    crypto_currency               crypto_currency,
    alias                         varchar,
    crypto_custody_account_id     uuid,
    portfolio_id                  uuid,
    created_on                    timestamp default now(),
    updated_on                    timestamp default now(),
    foreign key (portfolio_id) references portfolios (portfolio_id) on delete restrict on update restrict,
    foreign key (crypto_custody_account_id) references crypto_custody_accounts (crypto_custody_account_id) on delete restrict on update restrict
);

create table transactions
(
    transaction_id uuid primary key,
    timestamp      timestamp
);

create type operation_direction as enum (
    'CREDIT',
    'DEBIT'
    );

create table fiat_custody_operations
(
    operation_id            uuid primary key,
    amount                  bigint,
    operation_direction     operation_direction,
    transaction_id          uuid,
    fiat_custody_account_id uuid,
    foreign key (transaction_id) references transactions (transaction_id) on delete restrict on update restrict,
    foreign key (fiat_custody_account_id) references fiat_custody_accounts (fiat_custody_account_id) on delete restrict on update restrict
);

create table fiat_stakeholder_operations
(
    operation_id                uuid primary key,
    amount                      bigint,
    operation_direction         operation_direction,
    transaction_id              uuid,
    fiat_stakeholder_account_id uuid,
    foreign key (transaction_id) references transactions (transaction_id) on delete restrict on update restrict,
    foreign key (fiat_stakeholder_account_id) references fiat_stakeholder_accounts (fiat_stakeholder_account_id) on delete restrict on update restrict
);

create table crypto_custody_operations
(
    operation_id              uuid primary key,
    amount                    bigint,
    operation_direction       operation_direction,
    transaction_id            uuid,
    crypto_custody_account_id uuid,
    foreign key (transaction_id) references transactions (transaction_id) on delete restrict on update restrict,
    foreign key (crypto_custody_account_id) references crypto_custody_accounts (crypto_custody_account_id) on delete restrict on update restrict
);

create table crypto_stakeholder_operations
(
    operation_id                  uuid primary key,
    amount                        bigint,
    operation_direction           operation_direction,
    transaction_id                uuid,
    crypto_stakeholder_account_id uuid,
    foreign key (transaction_id) references transactions (transaction_id) on delete restrict on update restrict,
    foreign key (crypto_stakeholder_account_id) references crypto_stakeholder_accounts (crypto_stakeholder_account_id) on delete restrict on update restrict
);

commit transaction;