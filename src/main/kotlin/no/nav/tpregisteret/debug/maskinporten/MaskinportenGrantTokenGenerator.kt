package no.nav.tpregisteret.debug.maskinporten

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

internal const val SCOPE_CLAIM = "scope"
internal const val ONE_SECOND_IN_MILLISECONDS = 1000

internal const val PRIVATE_JWK_ENV_KEY = "jwk-private-key"
internal const val AUDIENCE_ENV_KEY = "aud_maskinporten"
internal const val ISSUER_ENV_KEY = "iss_maskinporten"
internal const val SCOPE_ENV_KEY = "scope_maskinporten"
internal const val VALID_IN_SECONDS_ENV_KEY = "jwt_expiration_time_seconds_maskinporten"

@Service
class MaskinportenGrantTokenGenerator(
    @Value("\${$PRIVATE_JWK_ENV_KEY:@null}")
    privateKeyString: String,

    @Value("\${$AUDIENCE_ENV_KEY:@null}")
    private val audience: String?,

    @Value("\${$ISSUER_ENV_KEY}")
    private val issuer: String,

    @Value("\${$SCOPE_ENV_KEY}")
    private val scope: String,

    @Value("\${$VALID_IN_SECONDS_ENV_KEY}")
    validInSecondString: String
) {
    private val validInSecond = validInSecondString.toInt()
    private val privateKey: RSAKey = RSAKey.parse(privateKeyString)

    internal val jwt: String
        get() = SignedJWT(signatureHeader, jwtClaimSet).apply {
            sign(RSASSASigner(privateKey))
        }.serialize()

    private val signatureHeader: JWSHeader
        get() = JWSHeader.Builder(JWSAlgorithm.RS256).keyID(privateKey.keyID).build()

    private val jwtClaimSet: JWTClaimsSet
        get() = JWTClaimsSet.Builder().apply {
            audience?.let(::audience)
            issuer(issuer)
            claim(SCOPE_CLAIM, scope)
            issueTime(Date())
            expirationTime(Date() addSeconds validInSecond)
        }.build()
}

infix fun Date.addSeconds(seconds: Int): Date = Date(time + seconds * ONE_SECOND_IN_MILLISECONDS)
