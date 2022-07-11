package no.arcane.platform.tests

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.util.*

const val fundId = "arcane-assets-fund-limited"

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
    ): String? {
        return apiClient.get {
            url(path = "apps/invest/funds")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
        }.body<Map<String, String>>()[fundId]
    }

    suspend fun sendFundInfoRequest(
        userId: String,
        fundInfoRequest: FundInfoRequest
    ): HttpStatusCode {
        return apiClient.post {
            url(path = "apps/invest/funds/$fundId")
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
        `when`("GET apps/invest/funds") {
            val status = getStatus(userId)
            then("Status should be NOT_REGISTERED") {
                status shouldBe "NOT_REGISTERED"
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
                    and("GET apps/invest/funds should be NOT_REGISTERED") {
                        getStatus(userId) shouldBe "NOT_REGISTERED"
                    }
                }
            }
        }
        `when`("POST /apps/invest/funds/$fundId") {
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
                then("Status should be 400 BadRequest") {
                    status shouldBe HttpStatusCode.BadRequest
                    and("GET apps/invest/funds should be NOT_REGISTERED") {
                        getStatus(userId) shouldBe "NOT_REGISTERED"
                    }
                }
            }
        }
        `when`("POST /apps/invest/funds/$fundId") {
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
                then("Status should be 403 Forbidden") {
                    status shouldBe HttpStatusCode.Forbidden
                    and("GET apps/invest/funds should be NOT_AUTHORIZED") {
                        getStatus(userId) shouldBe "NOT_AUTHORIZED"
                    }
                }
            }
        }
        `when`("POST /apps/invest/funds/$fundId") {
            and("Investor Type: Non professional") {
                val status = sendFundInfoRequest(
                    userId = userId,
                    fundInfoRequest = FundInfoRequest(
                        investorType = InvestorType.NON_PROFESSIONAL,
                    )
                )
                then("Status should be 403 Forbidden") {
                    status shouldBe HttpStatusCode.Forbidden
                    and("GET apps/invest/register should be NOT_AUTHORIZED") {
                        getStatus(userId) shouldBe "NOT_AUTHORIZED"
                    }
                }
            }
        }
        `when`("POST /apps/invest/funds/$fundId") {
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
            then("Status should be 200 OK") {
                status shouldBe HttpStatusCode.OK
                and("GET apps/invest/funds should be REGISTERED") {
                    getStatus(userId) shouldBe "REGISTERED"
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