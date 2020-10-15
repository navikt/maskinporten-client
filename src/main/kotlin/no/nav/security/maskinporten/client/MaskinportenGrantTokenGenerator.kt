package no.nav.security.maskinporten.client

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.util.*

class MaskinportenGrantTokenGenerator(
    private val config: MaskinportenConfig
) {

    internal val jwt: String
        get() = SignedJWT(signatureHeader, jwtClaimSet).apply {
            sign(RSASSASigner(config.privateKey))
        }.serialize()

    private val signatureHeader: JWSHeader
        get() = JWSHeader.Builder(JWSAlgorithm.RS256).keyID(config.privateKey.keyID).build()

    private val jwtClaimSet: JWTClaimsSet
        get() = JWTClaimsSet.Builder().apply {
            audience(config.issuer)
            issuer(config.clientId)
            claim(SCOPE_CLAIM, config.scope)
            issueTime(Date())
            expirationTime(Date() addSeconds config.validInSeconds)
        }.build()
}
