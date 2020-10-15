package no.nav.security.maskinporten.client

import com.nimbusds.jwt.SignedJWT
import java.util.*


internal class TokenCache(private val token: String? = null) {

    internal val tokenString: String
        get() = token!!

    internal val isExpired: Boolean
        get() = token == null || !token.tokenExpirationTime.is20SecondsPrior

    private val Date.is20SecondsPrior: Boolean
        get() = epochSeconds - (now.epochSeconds + TWENTY_SECONDS) >= 0

    private val Date.epochSeconds: Long
        get() = time / 1000

    private val now: Date
        get() = Date()

    private val String.tokenExpirationTime: Date
        get() = SignedJWT.parse(this).jwtClaimsSet.expirationTime

    companion object {
        private const val TWENTY_SECONDS = 20
    }
}
