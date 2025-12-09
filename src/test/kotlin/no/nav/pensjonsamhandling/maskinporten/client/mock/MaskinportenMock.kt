package no.nav.pensjonsamhandling.maskinporten.client.mock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.stubbing.Scenario
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.pensjonsamhandling.maskinporten.client.MaskinportenClient.Companion.CONTENT_TYPE
import no.nav.pensjonsamhandling.maskinporten.client.MaskinportenClient.Companion.GRANT_TYPE
import no.nav.pensjonsamhandling.maskinporten.client.MaskinportenConfig
import java.util.*

internal class MaskinportenMock {
    private var mock = WireMockServer(PORT)
    private val privateKey: RSAKey = RSAKeyGenerator(2048).keyID("123").generate()

    init {
        mock.start()
    }

    internal fun reset() {
        mock.resetAll()
    }

    internal fun stop() {
        mock.stop()
    }

    internal fun `mock  maskinporten token enpoint`() {
        mock.stubFor(WireMock.post(WireMock.urlPathEqualTo(TOKEN_PATH))
                .withHeader("Content-Type", WireMock.equalTo(CONTENT_TYPE))
                .withRequestBody(WireMock.matchingJsonPath("$.grant_type", WireMock.matching(GRANT_TYPE)))
                .withRequestBody(WireMock.matchingJsonPath("$.assertion"))
                .willReturn(WireMock.ok("""{
                      "access_token" : "${createMaskinportenToken()}",
                      "token_type" : "Bearer",
                      "expires_in" : 599,
                      "scope" : "difitest:test1"
                    }
                """)))
    }

    internal fun `mock valid response for only one call`() {
        mock.stubFor(WireMock.post(WireMock.urlPathEqualTo(TOKEN_PATH))
                .withHeader("Content-Type", WireMock.equalTo(CONTENT_TYPE))
                .inScenario("First time")
                .whenScenarioStateIs(Scenario.STARTED)
                .withRequestBody(WireMock.matching("grant_type=$GRANT_TYPE&assertion=.*"))
                .willReturn(WireMock.ok("""{
                      "access_token" : "${createMaskinportenToken()}",
                      "token_type" : "Bearer",
                      "expires_in" : 599,
                      "scope" : "difitest:test1"
                    }
                """))
                .willSetStateTo("Ended"))
    }

    internal fun `mock invalid JSON response`() {
        mock.stubFor(WireMock.post(WireMock.urlPathEqualTo(TOKEN_PATH))
                .withHeader("Content-Type", WireMock.equalTo(CONTENT_TYPE))
                .withRequestBody(WireMock.matching("grant_type=$GRANT_TYPE&assertion=.*"))
                .willReturn(WireMock.ok("""w""")))
    }

    internal fun `mock 500 server error`() {
        mock.stubFor(WireMock.post(WireMock.urlPathEqualTo(TOKEN_PATH))
                .withHeader("Content-Type", WireMock.equalTo(CONTENT_TYPE))
                .withRequestBody(WireMock.matching("grant_type=$GRANT_TYPE&assertion=.*"))
                .willReturn(WireMock.serverError().withBody("test body")))
    }


    private fun createMaskinportenToken(): String {
        val claimsSet = JWTClaimsSet.Builder()
                .subject("alice")
                .issuer("https://c2id.com")
                .expirationTime(Date(Date().time + (60 * 1000)))
                .build()
        val signedJWT = SignedJWT(
                JWSHeader.Builder(JWSAlgorithm.RS256).keyID(privateKey.keyID).build(),
                claimsSet)
        val signer: JWSSigner = RSASSASigner(privateKey)
        signedJWT.sign(signer)
        return signedJWT.serialize()
    }

    companion object {

        private const val PORT = 8096
        private const val TOKEN_PATH = "/token"
        private const val MASKINPORTEN_MOCK_HOST = "http://localhost:$PORT"

        internal fun createMaskinportenConfig(privateKey: RSAKey = RSAKeyGenerator(2048).keyID("123").generate()) = MaskinportenConfig(
                baseUrl = MASKINPORTEN_MOCK_HOST,
                clientId = "17b3e4e8-8203-4463-a947-5c24021b7742",
                privateKey = privateKey,
                expiresInSeconds = 120
        )
    }
}
