package no.nav.pensjonsamhandling.maskinporten.client

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.util.*

class MaskinportenGrantTokenGenerator(
    private val config: MaskinportenConfig
) {
    internal fun generateJWT(scopes: List<String>) = SignedJWT(signatureHeader, generateJWTClaimSet(scopes)).apply {
        sign(RSASSASigner(config.privateKey))
    }.serialize()

    private val signatureHeader: JWSHeader
        get() = JWSHeader.Builder(JWSAlgorithm.RS256).keyID(config.privateKey.keyID).build()

    private fun generateJWTClaimSet(scopes: List<String>) = JWTClaimsSet.Builder().apply {
        audience(config.issuer)
        issuer(config.clientId)
        claim(SCOPE_CLAIM, scopes.joinToString(" "))
        config.jti?.also { claim(JTI_CLAIM, it) }
        config.resource?.also { claim(RESOURCE_CLAIM, it) }
        issueTime(Date())
        expirationTime(Date() addSeconds config.validInSeconds)
    }.build()

    companion object {
        internal const val JTI_CLAIM = "jti"
        internal const val SCOPE_CLAIM = "scope"
        internal const val RESOURCE_CLAIM = "resource"
    }
}
