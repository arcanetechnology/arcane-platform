CREATE TABLE Transactions
(
    TransactionId STRING(36)  NOT NULL,
    CreatedOn     TIMESTAMP   NOT NULL,
) PRIMARY KEY(TransactionId);

CREATE TABLE VirtualAccountOperations
(
    TransactionId    STRING(36)  NOT NULL,
    VirtualAccountId STRING(36)  NOT NULL,
    Amount           INT64       NOT NULL,
    Currency         STRING(64)  NOT NULL,
    CreatedOn        TIMESTAMP   NOT NULL,
) PRIMARY KEY(TransactionId, VirtualAccountId),
    INTERLEAVE IN PARENT Transactions ON DELETE CASCADE;

CREATE TABLE FiatCustodyAccounts
(
    FiatCustodyAccountId STRING(36)  NOT NULL,
    Balance              INT64       NOT NULL,
    ReservedBalance      INT64       NOT NULL,
    Currency             STRING(3)   NOT NULL,
    Alias                STRING(256) NOT NULL,
    CreatedOn            TIMESTAMP   NOT NULL,
    UpdatedOn            TIMESTAMP   NOT NULL,
    CONSTRAINT fiat_custody_balance_not_negative CHECK(Balance >= 0),
    CONSTRAINT fiat_custody_reserved_balance_not_negative CHECK(ReservedBalance >= 0),
) PRIMARY KEY(FiatCustodyAccountId);

CREATE TABLE FiatCustodyAccountOperations
(
    FiatCustodyAccountId STRING(36)  NOT NULL,
    TransactionId        STRING(36)  NOT NULL,
    Amount               INT64       NOT NULL,
    Balance              INT64       NOT NULL,
    CreatedOn            TIMESTAMP   NOT NULL,
    FOREIGN KEY (TransactionId) REFERENCES Transactions (TransactionId),
) PRIMARY KEY(FiatCustodyAccountId, TransactionId),
    INTERLEAVE IN PARENT FiatCustodyAccounts ON DELETE CASCADE;

CREATE TABLE CryptoCustodyAccounts
(
    CryptoCustodyAccountId STRING(36)  NOT NULL,
    Balance                INT64       NOT NULL,
    ReservedBalance        INT64       NOT NULL,
    CryptoCurrency         STRING(64)  NOT NULL,
    Alias                  STRING(256) NOT NULL,
    CreatedOn              TIMESTAMP   NOT NULL,
    UpdatedOn              TIMESTAMP   NOT NULL,
    CONSTRAINT crypto_custody_balance_not_negative CHECK(Balance >= 0),
    CONSTRAINT crypto_custody_reserved_balance_not_negative CHECK(ReservedBalance >= 0),
) PRIMARY KEY(CryptoCustodyAccountId);

CREATE TABLE CryptoCustodyAccountOperations
(
    CryptoCustodyAccountId STRING(36)  NOT NULL,
    TransactionId          STRING(36)  NOT NULL,
    Amount                 INT64       NOT NULL,
    Balance                INT64       NOT NULL,
    CreatedOn              TIMESTAMP   NOT NULL,
    FOREIGN KEY (TransactionId) REFERENCES Transactions (TransactionId),
) PRIMARY KEY(CryptoCustodyAccountId, TransactionId),
    INTERLEAVE IN PARENT CryptoCustodyAccounts ON DELETE CASCADE;

CREATE TABLE Users
(
    UserId    STRING(36) NOT NULL,
    CreatedOn TIMESTAMP  NOT NULL,
) PRIMARY KEY(UserId);

CREATE TABLE Profiles
(
    UserId    STRING(36)  NOT NULL,
    ProfileId STRING(36)  NOT NULL,
    Alias     STRING(256) NOT NULL,
    Type      STRING(8)   NOT NULL,
    CreatedOn TIMESTAMP   NOT NULL,
    UpdatedOn TIMESTAMP   NOT NULL,
) PRIMARY KEY(UserId, ProfileId),
  INTERLEAVE IN PARENT Users ON DELETE CASCADE;

CREATE TABLE FiatStakeholderAccounts
(
    UserId                   STRING(36)  NOT NULL,
    ProfileId                STRING(36)  NOT NULL,
    FiatStakeholderAccountId STRING(36)  NOT NULL,
    Balance                  INT64       NOT NULL,
    ReservedBalance          INT64       NOT NULL,
    Currency                 STRING(3)   NOT NULL,
    Alias                    STRING(256) NOT NULL,
    FiatCustodyAccountId     STRING(36)  NOT NULL,
    CreatedOn                TIMESTAMP   NOT NULL,
    UpdatedOn                TIMESTAMP   NOT NULL,
    FOREIGN KEY (FiatCustodyAccountId) REFERENCES FiatCustodyAccounts (FiatCustodyAccountId),
    CONSTRAINT fiat_stakeholder_balance_not_negative CHECK(Balance >= 0),
    CONSTRAINT fiat_stakeholder_reserved_balance_not_negative CHECK(ReservedBalance >= 0),
) PRIMARY KEY(UserId, ProfileId, FiatStakeholderAccountId),
  INTERLEAVE IN PARENT Profiles ON DELETE CASCADE;

CREATE TABLE FiatStakeholderAccountOperations
(
    UserId                   STRING(36)  NOT NULL,
    ProfileId                STRING(36)  NOT NULL,
    FiatStakeholderAccountId STRING(36)  NOT NULL,
    TransactionId            STRING(36)  NOT NULL,
    Amount                   INT64       NOT NULL,
    Balance                  INT64       NOT NULL,
    CreatedOn                TIMESTAMP   NOT NULL,
    FOREIGN KEY (TransactionId) REFERENCES Transactions (TransactionId),
) PRIMARY KEY(UserId, ProfileId, FiatStakeholderAccountId, TransactionId),
    INTERLEAVE IN PARENT FiatStakeholderAccounts ON DELETE CASCADE;

CREATE TABLE CryptoStakeholderAccounts
(
    UserId                     STRING(36)      NOT NULL,
    ProfileId                  STRING(36)      NOT NULL,
    CryptoStakeholderAccountId STRING(36)      NOT NULL,
    Balance                    INT64           NOT NULL,
    ReservedBalance            INT64           NOT NULL,
    CryptoCurrency             STRING(64)      NOT NULL,
    Alias                      STRING(256)     NOT NULL,
    CryptoCustodyAccountId     STRING(36)      NOT NULL,
    CreatedOn                  TIMESTAMP       NOT NULL,
    UpdatedOn                  TIMESTAMP       NOT NULL,
    FOREIGN KEY (CryptoCustodyAccountId) REFERENCES CryptoCustodyAccounts (CryptoCustodyAccountId),
    CONSTRAINT crypto_stakeholder_balance_not_negative CHECK(Balance >= 0),
    CONSTRAINT crypto_stakeholder_reserved_balance_not_negative CHECK(ReservedBalance >= 0),
) PRIMARY KEY(UserId, ProfileId, CryptoStakeholderAccountId),
  INTERLEAVE IN PARENT Profiles ON DELETE CASCADE;

CREATE TABLE CryptoStakeholderAccountOperations
(
    UserId                     STRING(36)  NOT NULL,
    ProfileId                  STRING(36)  NOT NULL,
    CryptoStakeholderAccountId STRING(36)  NOT NULL,
    TransactionId              STRING(36)  NOT NULL,
    Amount                     INT64       NOT NULL,
    Balance                    INT64       NOT NULL,
    CreatedOn                  TIMESTAMP   NOT NULL,
    FOREIGN KEY (TransactionId) REFERENCES Transactions (TransactionId),
) PRIMARY KEY(UserId, ProfileId, CryptoStakeholderAccountId, TransactionId),
    INTERLEAVE IN PARENT CryptoStakeholderAccounts ON DELETE CASCADE;

CREATE TABLE Portfolios
(
    UserId                   STRING(36)  NOT NULL,
    ProfileId                STRING(36)  NOT NULL,
    FiatStakeholderAccountId STRING(36)  NOT NULL,
    PortfolioId              STRING(36)  NOT NULL,
    Alias                    STRING(256) NOT NULL,
    CreatedOn                TIMESTAMP   NOT NULL,
    UpdatedOn                TIMESTAMP   NOT NULL,
) PRIMARY KEY(UserId, ProfileId, FiatStakeholderAccountId, PortfolioId),
  INTERLEAVE IN PARENT FiatStakeholderAccounts ON DELETE CASCADE;

CREATE TABLE PortfolioCryptoStakeholderAccounts
(
    UserId                              STRING(36)  NOT NULL,
    ProfileId                           STRING(36)  NOT NULL,
    FiatStakeholderAccountId            STRING(36)  NOT NULL,
    PortfolioId                         STRING(36)  NOT NULL,
    PortfolioCryptoStakeholderAccountId STRING(36)  NOT NULL,
    Balance                             INT64       NOT NULL,
    ReservedBalance                     INT64       NOT NULL,
    CryptoCurrency                      STRING(64)  NOT NULL,
    Alias                               STRING(256) NOT NULL,
    CryptoCustodyAccountId              STRING(36)  NOT NULL,
    CreatedOn                           TIMESTAMP   NOT NULL,
    UpdatedOn                           TIMESTAMP   NOT NULL,
    FOREIGN KEY (CryptoCustodyAccountId) REFERENCES CryptoCustodyAccounts (CryptoCustodyAccountId),
    CONSTRAINT portfolio_crypto_stakeholder_balance_not_negative CHECK(Balance >= 0),
    CONSTRAINT portfolio_crypto_stakeholder_reserved_balance_not_negative CHECK(ReservedBalance >= 0),
) PRIMARY KEY(UserId, ProfileId, FiatStakeholderAccountId, PortfolioId, PortfolioCryptoStakeholderAccountId),
  INTERLEAVE IN PARENT Portfolios ON DELETE CASCADE;

CREATE TABLE PortfolioCryptoStakeholderAccountOperations
(
    UserId                              STRING(36)  NOT NULL,
    ProfileId                           STRING(36)  NOT NULL,
    FiatStakeholderAccountId            STRING(36)  NOT NULL,
    PortfolioId                         STRING(36)  NOT NULL,
    PortfolioCryptoStakeholderAccountId STRING(36)  NOT NULL,
    TransactionId                       STRING(36)  NOT NULL,
    Amount                              INT64       NOT NULL,
    Balance                             INT64       NOT NULL,
    CreatedOn                           TIMESTAMP   NOT NULL,
    FOREIGN KEY (TransactionId) REFERENCES Transactions (TransactionId),
) PRIMARY KEY(UserId, ProfileId, FiatStakeholderAccountId, PortfolioId, PortfolioCryptoStakeholderAccountId, TransactionId),
    INTERLEAVE IN PARENT PortfolioCryptoStakeholderAccounts ON DELETE CASCADE;