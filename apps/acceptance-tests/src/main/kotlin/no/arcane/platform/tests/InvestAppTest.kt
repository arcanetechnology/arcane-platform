package no.arcane.platform.tests

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.util.*

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
    ): HttpStatusCode {
        return apiClient.get {
            url(path = "apps/invest/register")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
            expectSuccess = false
        }.status
    }

    suspend fun sendFundInfoRequest(
        userId: String,
        fundInfoRequest: FundInfoRequest
    ): HttpStatusCode {
        return apiClient.post {
            url(path = "apps/invest/register")
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
        `when`("GET apps/invest/register") {
            val status = getStatus(userId)
            then("Status should be 404") {
                status shouldBe HttpStatusCode.NotFound
            }
        }
        `when`("POST /apps/invest/register") {
            and("Missing mandatory fields") {
                val status = sendFundInfoRequest(
                    userId = userId,
                    fundInfoRequest = FundInfoRequest(
                        investorType = InvestorType.PROFESSIONAL,
                    )
                )
                then("Status should be 400") {
                    status shouldBe HttpStatusCode.BadRequest
                    and("GET apps/invest/register should be 404") {
                        getStatus(userId) shouldBe HttpStatusCode.NotFound
                    }
                }
            }
        }
        `when`("POST /apps/invest/register") {
            and("Invalid phone number") {
                val status = sendFundInfoRequest(
                    userId = userId,
                    fundInfoRequest = FundInfoRequest(
                        investorType = InvestorType.PROFESSIONAL,
                        name = "Test",
                        phoneNumber = invalidPhoneNumber,
                        countryCode = "NOR",
                        fundName = "Arcane Assets Fund Limited"
                    )
                )
                then("Status should be 400") {
                    status shouldBe HttpStatusCode.BadRequest
                    and("GET apps/invest/register should be 404") {
                        getStatus(userId) shouldBe HttpStatusCode.NotFound
                    }
                }
            }
        }
        `when`("POST /apps/invest/register") {
            and("Incorrect fund name") {
                val status = sendFundInfoRequest(
                    userId = userId,
                    fundInfoRequest = FundInfoRequest(
                        investorType = InvestorType.PROFESSIONAL,
                        name = "Test",
                        phoneNumber = validPhoneNumber,
                        countryCode = "NOR",
                        fundName = "Arcane Fund"
                    )
                )
                then("Status should be 403") {
                    status shouldBe HttpStatusCode.Forbidden
                    and("GET apps/invest/register should be 404") {
                        getStatus(userId) shouldBe HttpStatusCode.Forbidden
                    }
                }
            }
        }
        `when`("POST /apps/invest/register") {
            and("Investor Type: Non professional") {
                val status = sendFundInfoRequest(
                    userId = userId,
                    fundInfoRequest = FundInfoRequest(
                        investorType = InvestorType.NON_PROFESSIONAL,
                    )
                )
                then("Status should be 403") {
                    status shouldBe HttpStatusCode.Forbidden
                    and("GET apps/invest/register should be 404") {
                        getStatus(userId) shouldBe HttpStatusCode.Forbidden
                    }
                }
            }
        }
        `when`("POST /apps/invest/register") {
            val status = sendFundInfoRequest(
                userId = userId,
                fundInfoRequest = FundInfoRequest(
                    investorType = InvestorType.PROFESSIONAL,
                    name = "Test",
                    phoneNumber = validPhoneNumber,
                    countryCode = "NOR",
                    fundName = "Arcane Assets Fund Limited"
                )
            )
            then("Status should be 200") {
                status shouldBe HttpStatusCode.OK
                and("GET apps/invest/register should be 200") {
                    getStatus(userId) shouldBe HttpStatusCode.OK
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