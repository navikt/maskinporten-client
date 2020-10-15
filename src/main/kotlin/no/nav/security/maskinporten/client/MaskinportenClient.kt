package no.nav.security.maskinporten.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.security.maskinporten.client.exceptions.MaskinportenClientException
import no.nav.security.maskinporten.client.exceptions.MaskinportenObjectMapperException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers.ofString
import java.net.http.HttpResponse.BodyHandlers.ofString


class MaskinportenClient(
        private val config: MaskinportenConfig
) {
    private var tokenCache: TokenCache = TokenCache()
    private val grantTokenGenerator = MaskinportenGrantTokenGenerator(config)

    private val httpClient: HttpClient = HttpClient.newHttpClient()
    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    internal val maskinportenToken: String
        get() = (tokenCache.takeUnless(TokenCache::isExpired)
                ?: TokenCache(tokenFromMaskinporten).also { tokenCache = it }
                ).tokenString


    private val tokenFromMaskinporten: String
        get() = httpClient.send(tokenRequest, ofString()).run {
            if (statusCode() != 200) throw MaskinportenClientException(this)
            mapToMaskinportenResponseBody(body()).access_token
        }

    private val tokenRequest: HttpRequest
        get() = HttpRequest.newBuilder()
                .uri(URI.create(config.baseUrl + MASKINPORTEN_TOKEN_PATH))
                .header("Content-Type", CONTENT_TYPE)
                .POST(ofString(requestBody))
                .build()

    private val requestBody: String
        get() = objectMapper.writeValueAsString(MaskinportenRequestBody(assertion = grantTokenGenerator.jwt))

    private fun mapToMaskinportenResponseBody(responseBody: String): MaskinportenResponseBody =
            try {
                objectMapper.readValue(responseBody)
            } catch (e: Exception) {
                throw MaskinportenObjectMapperException(e.toString())
            }

    companion object {
        internal const val MASKINPORTEN_TOKEN_PATH = "/token"

        internal const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer"
        internal const val CONTENT_TYPE = "application/x-www-form-urlencoded"
    }

    internal data class MaskinportenRequestBody(val grant_type: String = GRANT_TYPE, val assertion: String)
    internal data class MaskinportenResponseBody(val access_token: String, val token_type: String?, val expires_in: Int?, val scope: String?)
}
