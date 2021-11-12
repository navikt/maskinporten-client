package no.nav.pensjonsamhandling.maskinporten.client

import com.nimbusds.jwt.SignedJWT
import java.util.*


internal class TokenCache(token: String) {
    internal var token: SignedJWT? = SignedJWT.parse(token)
        get() = field?.takeUnless { it.isExpired }

    private val SignedJWT.isExpired: Boolean
        get() = jwtClaimsSet?.expirationTime?.is20SecondsPrior?.not() ?: false

    private val Date.is20SecondsPrior: Boolean
        get() = epochSeconds - (now.epochSeconds + TWENTY_SECONDS) >= 0

    private val Date.epochSeconds: Long
        get() = time / 1000

    private val now: Date
        get() = Date()

    internal fun renew(newToken: String) = SignedJWT.parse(newToken).also {
        token = it
    }

    companion object {
        private const val TWENTY_SECONDS = 20
    }
}
