package com.k33.platform.tests

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.util.*

const val fundId = "k33-assets-i-fund-limited"
const val invalidFundId = "k33-fund"

class InvestAppTest : BehaviorSpec({

    val validPhoneNumber = PhoneNumber(
        countryCode = "47",
        nationalNumber = "41234567"
    )

    val invalidPhoneNumber = PhoneNumber(
        countryCode = "47",
        nationalNumber = "12345678"
    )

    suspend fun getStatus(
        userId: String,
        fundIdValue: String = fundId,
    ): HttpStatusCode {
        return apiClient.get {
            url(path = "apps/invest/funds/$fundIdValue")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
        }.status
    }

    suspend fun getStatusMap(
        userId: String,
    ): Map<String, String> {
        return apiClient.get {
            url(path = "apps/invest/funds")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
        }.body()
    }

    suspend fun sendFundInfoRequest(
        userId: String,
        fundInfoRequest: FundInfoRequest,
        fundIdValue: String = fundId,
    ): HttpStatusCode {
        return apiClient.put {
            url(path = "apps/invest/funds/$fundIdValue")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
            contentType(ContentType.Application.Json)
            setBody(fundInfoRequest)
            expectSuccess = false
        }.status
    }

    given("User is not registered") {
        val userId = UUID.randomUUID().toString()
        `when`("GET apps/invest/funds/$fundId") {
            then("Status should be 404 NOT FOUND") {
                getStatus(userId) shouldBe HttpStatusCode.NotFound

            }
            then("GET apps/invest/funds should be NOT_REGISTERED") {
                getStatusMap(userId) shouldBe mapOf(fundId to "NOT_REGISTERED")
            }
        }
        `when`("PUT /apps/invest/funds/$invalidFundId with Invalid Fund ID") {
            val status = sendFundInfoRequest(
                userId = userId,
                fundInfoRequest = FundInfoRequest(
                    investorType = InvestorType.NON_PROFESSIONAL,
                ),
                fundIdValue = invalidFundId
            )
            then("Status should be 404 Not Found") {
                status shouldBe HttpStatusCode.NotFound
            }
        }
        `when`("GET /apps/invest/funds/$invalidFundId with Invalid Fund ID") {
            val status = getStatus(
                userId = userId,
                fundIdValue = invalidFundId
            )
            then("Status should be 404 Not Found") {
                status shouldBe HttpStatusCode.NotFound
            }
        }
        `when`("POST /apps/invest/funds/$fundId") {
            and("Missing mandatory fields") {
                val status = sendFundInfoRequest(
                    userId = userId,
                    fundInfoRequest = FundInfoRequest(
                        investorType = InvestorType.PROFESSIONAL,
                    )
                )
                then("Status should be 400 BadRequest") {
                    status shouldBe HttpStatusCode.BadRequest
                }
                then("GET apps/invest/funds/$fundId should be 404 NOT FOUND") {
                    getStatus(userId) shouldBe HttpStatusCode.NotFound
                }
                then("GET apps/invest/funds should be NOT_REGISTERED") {
                    getStatusMap(userId) shouldBe mapOf(fundId to "NOT_REGISTERED")
                }
            }
            and("Invalid phone number") {
                val status = sendFundInfoRequest(
                    userId = userId,
                    fundInfoRequest = FundInfoRequest(
                        investorType = InvestorType.PROFESSIONAL,
                        name = "Test",
                        phoneNumber = invalidPhoneNumber,
                        countryCode = "NOR",
                        fundName = "K33 Assets I Fund Limited"
                    )
                )
                then("Status should be 400 BadRequest") {
                    status shouldBe HttpStatusCode.BadRequest
                }
                then("GET apps/invest/funds/$fundId should be 404 NOT FOUND") {
                    getStatus(userId) shouldBe HttpStatusCode.NotFound
                }
                then("GET apps/invest/funds should be NOT_REGISTERED") {
                    getStatusMap(userId) shouldBe mapOf(fundId to "NOT_REGISTERED")
                }
            }
            and("Incorrect fund name") {
                val status = sendFundInfoRequest(
                    userId = userId,
                    fundInfoRequest = FundInfoRequest(
                        investorType = InvestorType.PROFESSIONAL,
                        name = "Test",
                        phoneNumber = validPhoneNumber,
                        countryCode = "NOR",
                        fundName = "K33 Fund"
                    )
                )
                then("Status should be 403 Forbidden") {
                    status shouldBe HttpStatusCode.Forbidden
                }
                then("GET apps/invest/funds/$fundId should be 403 FORBIDDEN") {
                    getStatus(userId) shouldBe HttpStatusCode.Forbidden
                }
                then("GET apps/invest/funds should be NOT_AUTHORIZED") {
                    getStatusMap(userId) shouldBe mapOf(fundId to "NOT_AUTHORIZED")
                }
            }
            and("Investor Type: Non professional") {
                val status = sendFundInfoRequest(
                    userId = userId,
                    fundInfoRequest = FundInfoRequest(
                        investorType = InvestorType.NON_PROFESSIONAL,
                    )
                )
                then("Status should be 403 Forbidden") {
                    status shouldBe HttpStatusCode.Forbidden
                }
                then("GET apps/invest/funds/$fundId should be 403 FORBIDDEN") {
                    getStatus(userId) shouldBe HttpStatusCode.Forbidden
                }
                then("GET apps/invest/funds should be NOT_AUTHORIZED") {
                    getStatusMap(userId) shouldBe mapOf(fundId to "NOT_AUTHORIZED")
                }
            }
            and("with Valid FundInfoRequest") {
                val status = sendFundInfoRequest(
                    userId = userId,
                    fundInfoRequest = FundInfoRequest(
                        investorType = InvestorType.PROFESSIONAL,
                        name = "Test",
                        phoneNumber = validPhoneNumber,
                        countryCode = "NOR",
                        fundName = "K33 Assets I Fund Limited"
                    )
                )
                then("Status should be 200 OK") {
                    status shouldBe HttpStatusCode.OK
                }
                then("GET apps/invest/funds/$fundId should be 200 OK") {
                    getStatus(userId) shouldBe HttpStatusCode.OK
                }
                then("GET apps/invest/funds should be REGISTERED") {
                    getStatusMap(userId) shouldBe mapOf(fundId to "REGISTERED")
                }
            }
        }
    }
})

enum class InvestorType {
    PROFESSIONAL,
    ELECTIVE_PROFESSIONAL,
    NON_PROFESSIONAL,
}

@Serializable
data class FundInfoRequest(
    val investorType: InvestorType,
    val name: String? = null,
    val company: String? = null,
    val phoneNumber: PhoneNumber? = null,
    val countryCode: String? = null, // ISO 3166 3 character alpha-3 code
    val fundName: String? = null,
)

@Serializable
data class PhoneNumber(
    val countryCode: String,
    val nationalNumber: String,
)