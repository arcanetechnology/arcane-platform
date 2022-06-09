INSERT INTO FiatCustodyAccounts
    (FiatCustodyAccountId, Balance, ReservedBalance, Currency, Alias, CreatedOn, UpdatedOn)
VALUES ("real-chf-sp1", 1000000, 0, "CHF", "Real CHF SP1", CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
       ("real-dkk-sp1", 1000000, 0, "DKK", "Real DKK SP1", CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
       ("real-eur-sp1", 1000000, 0, "EUR", "Real EUR SP1", CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
       ("real-gbp-sp1", 1000000, 0, "GBP", "Real GBP SP1", CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
       ("real-nok-sp1", 1000000, 0, "NOK", "Real NOK SP1", CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
       ("real-sek-sp1", 1000000, 0, "SEK", "Real SEK SP1", CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
       ("real-usd-sp1", 1000000, 0, "USD", "Real USD SP1", CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO CryptoCustodyAccounts
    (CryptoCustodyAccountId, Balance, ReservedBalance, CryptoCurrency, Alias, CreatedOn, UpdatedOn)
VALUES ("real-eth-coinbase", 1000000, 0, "ETH", "Real ETH Coinbase", CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
       ("real-eth-metamask", 1000000, 0, "ETH", "Real ETH Metamask", CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
       ("real-matic-ftx", 1000000, 0, "MATIC", "Real MATIC FTX", CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
       ("real-matic-metamask", 1000000, 0, "MATIC", "Real MATIC Metamask", CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());