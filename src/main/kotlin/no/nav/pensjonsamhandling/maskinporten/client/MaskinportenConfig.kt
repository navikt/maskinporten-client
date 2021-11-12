package no.nav.pensjonsamhandling.maskinporten.client

import com.nimbusds.jose.jwk.RSAKey
import java.net.ProxySelector

data class MaskinportenConfig(
    internal val baseUrl: String,
    internal val clientId: String,
    internal val privateKey: RSAKey,
    internal val validInSeconds: Int,
    internal val proxy: ProxySelector = ProxySelector.getDefault(),
    internal val jti: String? = null,
    internal val resource: String? = null
) {
    internal val issuer = baseUrl.suffix("/")
}