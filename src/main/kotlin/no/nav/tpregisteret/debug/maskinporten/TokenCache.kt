package no.nav.tpregisteret.debug.maskinporten

import com.nimbusds.jwt.SignedJWT
import java.util.*

private const val TWENTY_SECONDS = 20

internal class TokenCache(private val token: String? = null) {

    internal val tokenString: String
        get() = token!!

    internal val isExpired: Boolean
        get() = token == null || !token.getTokenExpirationTime().is20SecondsEarlierThenNow()
}


private fun Date.is20SecondsEarlierThenNow(): Boolean =
    timeInSeconds() - (now().timeInSeconds() + TWENTY_SECONDS) >= 0

private fun Date.timeInSeconds() = this.time / 1000
private fun now() = Date()
private fun String.getTokenExpirationTime() = SignedJWT.parse(this).jwtClaimsSet.expirationTime as Date
