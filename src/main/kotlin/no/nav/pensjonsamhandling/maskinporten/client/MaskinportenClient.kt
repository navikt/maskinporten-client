package no.nav.pensjonsamhandling.maskinporten.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nimbusds.jwt.SignedJWT
import no.nav.pensjonsamhandling.maskinporten.client.exceptions.MaskinportenClientException
import no.nav.pensjonsamhandling.maskinporten.client.exceptions.MaskinportenObjectMapperException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers.ofString
import java.net.http.HttpResponse.BodyHandlers.ofString


class MaskinportenClient(
    private val config: MaskinportenConfig
) {
    private var tokenCache: MutableMap<List<String>, TokenCache> = HashMap()
    private val grantTokenGenerator = MaskinportenGrantTokenGenerator(config)

    private val httpClient: HttpClient = HttpClient.newBuilder().proxy(config.proxy).build()
    private val objectMapper = ObjectMapper().registerKotlinModule()

    fun getToken(vararg scope: String): SignedJWT = tokenCache.getOrPut(scope.distinct().sorted()) {
        TokenCache(fetchToken(*scope))
    }.run {
        token ?: renew(fetchToken(*scope))
    }

    fun getTokenString(vararg scope: String) = getToken(*scope).parsedString!!

    private fun fetchToken(vararg scope: String) = httpClient.send(tokenRequest(*scope), ofString()).run {
        if (statusCode() != 200) throw MaskinportenClientException(this)
        mapToMaskinportenResponseBody(body()).access_token
    }

    private fun tokenRequest(vararg scope: String) = HttpRequest.newBuilder()
        .uri(URI.create(config.baseUrl + MASKINPORTEN_TOKEN_PATH))
        .header("Content-Type", CONTENT_TYPE)
        .POST(ofString(requestBody(grantTokenGenerator.generateJWT(scope.distinct()))))
        .build()

    private fun requestBody(jwt: String) = "grant_type=$GRANT_TYPE&assertion=$jwt"

    private fun mapToMaskinportenResponseBody(responseBody: String): MaskinportenResponseBody = try {
        objectMapper.readValue(responseBody)
    } catch (e: Exception) {
        throw MaskinportenObjectMapperException(e.toString())
    }

    companion object {
        internal const val MASKINPORTEN_TOKEN_PATH = "/token"

        internal const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer"
        internal const val CONTENT_TYPE = "application/x-www-form-urlencoded"
    }

    internal data class MaskinportenResponseBody(
        val access_token: String,
        val token_type: String?,
        val expires_in: Int?,
        val scope: String?
    )
}
