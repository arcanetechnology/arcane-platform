# Trade App

## Ledger

### Model  

#### Custody and Stakeholder accounts.

Custody account acts like a parent to multiple stakeholder accounts.  
Stakeholder accounts owns a stake in the holdings of custody account.  
Credit or Debit on a custody account has to correlate with credit or debit on a stakeholder account.  
Sum of balance of all stakeholder accounts is the balance of the parent custody account.  
The balance of custody account remains unchanged for the transfer of funds within its child stakeholder accounts.

#### Fiat Custody Account

This is a real bank account with fiat currency.  
This acts as a parent to `Fiat Stakeholder Accounts`.  

#### Crypto Custody Account 

This is a real crypto account with cryptocurrency.  
This acts as a parent to `Crypto Stakeholder Accounts`.  

#### User

This is person who trades on this platform.

#### Profile

A user can have multiple profiles.  
Profiles can be of these types:
* Personal
* Business

A user is allowed to have 1 personal and 5 business accounts.  
A user may delegate permission to another user to operate on his/her behalf. 

#### Fiat Stakeholder Account

A user profile can have multiple accounts, one per fiat currency. 

#### Portfolio

A fiat account can fund multiple investment portfolios.

#### Crypto Stakeholder Account

A portfolio can have multiple crypto accounts, one per cryptocurrency.

#### Transaction

A transaction is group of multiple operations, such that sum of all credits and debits for every currency is zero.

#### Operations

An operation can be of these 2 types:
* CREDIT
* DEBIT

An operation operates on an account.

