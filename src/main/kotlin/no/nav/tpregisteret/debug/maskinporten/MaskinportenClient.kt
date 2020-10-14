package no.nav.tpregisteret.debug.maskinporten

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers.ofString
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers.ofString

internal const val MASKINPORTEN_TOKEN_PATH = "/token"
internal const val MASKINPORTEN_TOKEN_HOST_ENV_KEY = "maskinporten-host"

internal const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer"
internal const val CONTENT_TYPE = "application/x-www-form-urlencoded"

@Service
class MaskinportenClient(
        @Value("\${$MASKINPORTEN_TOKEN_HOST_ENV_KEY}")
        private val host: String,
        private val grantTokenGenerator: MaskinportenGrantTokenGenerator
) {

    private val httpClient: HttpClient = HttpClient.newHttpClient()
    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    private var tokenCache: TokenCache = TokenCache()

    internal val maskinportenToken: String
        get() = (if (tokenCache.isExpired) TokenCache(tokenFromMaskinporten).also {
            tokenCache = it
        } else tokenCache).tokenString


    private val tokenFromMaskinporten: String
        get() = httpClient.send(tokenRequest, ofString()).run {
                if (statusCode() != 200) throw MaskinportenClientException(this)
                mapToMaskinportenResponseBody(body()).access_token
            }

    private val tokenRequest: HttpRequest
        get() = HttpRequest.newBuilder()
                .uri(URI.create(host + MASKINPORTEN_TOKEN_PATH))
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
}

internal data class MaskinportenRequestBody(val grant_type: String = GRANT_TYPE, val assertion: String)
internal data class MaskinportenResponseBody(val access_token: String, val token_type: String?, val expires_in: Int?, val scope: String?)

internal class MaskinportenClientException(response: HttpResponse<String>) : Exception("Feil ved henting av token: Status: ${response.statusCode()} , Body: ${response.body()}")
internal class MaskinportenObjectMapperException(message: String) : Exception("Feil ved deserialisering av response fra maskinporten: $message")

