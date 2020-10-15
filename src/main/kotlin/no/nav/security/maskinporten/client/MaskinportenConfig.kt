package no.nav.security.maskinporten.client

import com.nimbusds.jose.jwk.RSAKey

data class MaskinportenConfig(
        internal val baseUrl: String,
        internal val clientId: String,
        internal val privateKey: RSAKey,
        internal val scope: String,
        internal val validInSeconds: Int,
        internal val issuer: String = "$baseUrl/.well-known/oauth-authorization-server",
        internal val audience: String? = null
)