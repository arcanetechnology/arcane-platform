package no.arcane.platform.tests

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.util.*

class TradeAdminGraphqlTest : StringSpec({

    @Serializable
    data class GraphqlRequest(
        val query: String
    )

    @Serializable
    data class GraphqlResponse(
        val data: String,
        val errors: List<String>? = null,
    )

    suspend fun queryGraphqlEndpoint(): GraphqlResponse = apiClient.post {
        url(path = "apps/trade/admin/graphql")
        contentType(ContentType.Application.Json)
        setBody(
            GraphqlRequest(
                query = """
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
            )
        )
    }.body()

    "POST /graphql -> Test Data" {
        val response = queryGraphqlEndpoint()

        response.errors shouldBe null
        response.data shouldEqualJson """
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