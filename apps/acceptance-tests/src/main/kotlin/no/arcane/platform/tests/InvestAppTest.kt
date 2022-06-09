package no.arcane.platform.tests

import io.kotest.core.spec.style.StringSpec
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.util.*

class InvestAppTest : StringSpec({
    val userId = UUID.randomUUID().toString()

    "GET /apps/invest/register -> Check if unregistered app exists" {
        apiClient.get {
            url(path = "apps/invest/register")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
            expectSuccess = false
        }
    }

    "POST /apps/invest/register -> Unqualified investor" {
        apiClient.post {
            url(path = "apps/invest/register")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
            contentType(ContentType.Application.Json)
            setBody(
                FundInfoRequest(
                    investorType = InvestorType.UNQUALIFIED,
                    name = "Test",
                    phoneNumber = PhoneNumber(
                        countryCode = "47",
                        nationalNumber = "12345678"
                    ),
                    countryCode = "NOR",
                    fundName = "Test fund name"
                )
            )
            expectSuccess = false
        }
    }

    "POST /apps/invest/register -> Register app" {
        apiClient.post {
            url(path = "apps/invest/register")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
            contentType(ContentType.Application.Json)
            setBody(
                FundInfoRequest(
                    investorType = InvestorType.PROFESSIONAL,
                    name = "Test",
                    phoneNumber = PhoneNumber(
                        countryCode = "47",
                        nationalNumber = "12345678"
                    ),
                    countryCode = "NOR",
                    fundName = "Test fund name"
                )
            )
        }
    }

    "GET /apps/invest/register -> Check if registered app exists" {
        apiClient.get {
            url(path = "apps/invest/register")
            headers {
                appendEndpointsApiUserInfoHeader(userId)
            }
        }
    }
})

enum class InvestorType {
    PROFESSIONAL,
    ELECTIVE_PROFESSIONAL,
    UNQUALIFIED,
}

@Serializable
data class FundInfoRequest(
    val investorType: InvestorType,
    val name: String,
    val company: String? = null,
    val phoneNumber: PhoneNumber,
    val countryCode: String, // ISO 3166 3 character alpha-3 code
    val fundName: String,
)

@Serializable
data class PhoneNumber(
    val countryCode: String,
    val nationalNumber: String,
)