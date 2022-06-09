package no.arcane.platform.app.trade.admin.api.graphql

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import graphql.ExecutionInput
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GraphqlTest : StringSpec({

    "generate sdl".config(enabled = false) {
        TradeAdminGraphqlService.getSdl() shouldBe """
            type CryptoAccount {
              alias: String!
              balance: BigInteger!
              createdOn: DateTime!
              currency: String!
              id: ID!
              updatedOn: DateTime!
            }

            type CustodyAccount {
              alias: String!
              balance: BigInteger!
              createdOn: DateTime!
              currency: Currency!
              id: ID!
              updatedOn: DateTime!
            }

            type Portfolio {
              alias: String!
              createdOn: DateTime!
              cryptoAccounts: [CryptoAccount!]!
              id: ID!
              updatedOn: DateTime!
            }

            type Profile {
              accounts: [StakeholderAccount!]!
              alias: String!
              createdOn: DateTime!
              id: ID!
              type: ProfileType!
              updatedOn: DateTime!
            }

            type Query {
              cryptoCustodyAccounts: [CryptoAccount!]
              fiatCustodyAccounts: [CustodyAccount!]
              user(userId: String!): TradeUser
              users: [User!]
            }

            type StakeholderAccount {
              alias: String!
              balance: BigInteger!
              createdOn: DateTime!
              currency: Currency!
              id: ID!
              portfolios: [Portfolio!]!
              updatedOn: DateTime!
            }

            type TradeUser {
              createdOn: DateTime
              email: String!
              id: ID!
              profiles: [Profile]
            }

            type User {
              email: String!
              id: ID!
            }

            enum Currency {
              CHF
              DKK
              EUR
              GBP
              NOK
              SEK
              USD
            }

            enum ProfileType {
              BUSINESS
              PERSONAL
            }

            "An arbitrary precision signed integer"
            scalar BigInteger

            "An RFC-3339 compliant DateTime Scalar"
            scalar DateTime

        """.trimIndent()
    }

    "execute query".config(enabled = false) {
        val executionInput: ExecutionInput = ExecutionInput.newExecutionInput()
            .query(
                """
                {
                    users {
                        id
                        email
                    }
                    user(userId: "foo") {
                        id
                        email
                        profiles {
                            id
                            alias
                            type
                            accounts {
                                id
                                balance
                                currency
                                alias
                                portfolios {
                                    id
                                    alias
                                    cryptoAccounts {
                                        id
                                        balance
                                        currency
                                        alias
                                    }
                                }
                            }
                        }
                    }
                    fiatCustodyAccounts {
                        id
                        balance
                        currency
                        alias
                    }
                    cryptoCustodyAccounts {
                        id
                        balance
                        currency
                        alias
                    }
                }
                """.trimIndent()
            ).build()

        jacksonObjectMapper().writeValueAsString(
            TradeAdminGraphqlService.getGraphQL().execute(executionInput).getData()
        ) shouldEqualJson """
        {
          "users": [
            {
              "id": "",
              "email": ""
            }
          ],
          "user": {
            "id": "",
            "email": "",
            "profiles": [
              {
                "id": "",
                "alias": "",
                "type": "PERSONAL",
                "accounts": [
                  {
                    "id": "",
                    "balance": 0,
                    "currency": "NOK",
                    "alias": "",
                    "portfolios": [
                      {
                        "id": "",
                        "alias": "",
                        "cryptoAccounts": [
                          {
                            "id": "",
                            "balance": 0,
                            "currency": "BTC",
                            "alias": ""
                          }
                        ]
                      }
                    ]
                  }
                ]
              }
            ]
          },
          "fiatCustodyAccounts": [
            {
              "id": "",
              "balance": 0,
              "currency": "NOK",
              "alias": ""
            }
          ],
          "cryptoCustodyAccounts": [
            {
              "id": "",
              "balance": 0,
              "currency": "BTC",
              "alias": ""
            }
          ]
        }
        """.trimIndent()
    }
})