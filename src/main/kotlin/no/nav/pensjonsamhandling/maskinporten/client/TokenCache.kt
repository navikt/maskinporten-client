package no.nav.pensjonsamhandling.maskinporten.client

import com.nimbusds.jwt.SignedJWT
import java.time.Instant
import java.util.*


internal class TokenCache(token: String) {
    internal var token: SignedJWT? = SignedJWT.parse(token)
        get() = field?.takeUnless { it.isExpired }

    private val SignedJWT.isExpired: Boolean
        get() = jwtClaimsSet?.expirationTime?.is20SecondsPrior?.not() ?: false

    private val Date.is20SecondsPrior: Boolean
        get() = toInstant().isAfter(now.plusSeconds(20))

    private val now: Instant
        get() = Instant.now()

    internal fun renew(newToken: String) = SignedJWT.parse(newToken).also {
        token = it
    }
}
