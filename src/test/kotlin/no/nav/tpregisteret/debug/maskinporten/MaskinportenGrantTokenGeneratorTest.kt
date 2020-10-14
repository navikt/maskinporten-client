package no.nav.tpregisteret.debug.maskinporten

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.SignedJWT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.*
import kotlin.math.absoluteValue

const val SCOPE_CLAIM = "scope"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ActiveProfiles("ct")
internal class MaskinportenGrantTokenGeneratorTest {
    private val privateKey: RSAKey = RSAKeyGenerator(2048).keyID("123").generate()
    private val publicKey: RSAKey = privateKey.toPublicJWK()

    @Autowired
    private lateinit var tokenGenerator: MaskinportenGrantTokenGenerator

    @Test
    fun `Token is signed with private key in environment variables`() {
        val signedJWT = SignedJWT.parse(tokenGenerator.jwt)
        val verifier: JWSVerifier = RSASSAVerifier(publicKey)

        assertTrue(signedJWT.verify(verifier))
    }

    @Test
    fun `Algorithm in token header is rsa256`() {
        val signedJWT = SignedJWT.parse(tokenGenerator.jwt)

        assertEquals("RS256", (signedJWT.header.algorithm as JWSAlgorithm).name)
    }

    @Test
    fun `Required claims added to token body`() {
        val env = createMaskinportenEnvVariables(privateKey)
        val signedJWT = SignedJWT.parse(tokenGenerator.jwt)

        assertEquals(env[AUDIENCE_ENV_KEY], signedJWT.jwtClaimsSet.audience[0])
        assertEquals(env[ISSUER_ENV_KEY], signedJWT.jwtClaimsSet.issuer)
        assertEquals(env[SCOPE_ENV_KEY], signedJWT.jwtClaimsSet.claims[SCOPE_CLAIM])
    }

    @Test
    fun `Required timestamps are added to token body`() {
        val env = createMaskinportenEnvVariables(privateKey)
        val signedJWT = SignedJWT.parse(tokenGenerator.jwt)

        val issuedAt = signedJWT.jwtClaimsSet.issueTime as Date
        val expirationTime = signedJWT.jwtClaimsSet.expirationTime as Date

        assertTrue(Date() equalWithinOneSecond issuedAt)
        assertTrue((Date() addSeconds env.getValue(VALID_IN_SECONDS_ENV_KEY).toInt()) equalWithinOneSecond expirationTime)
    }
}

private infix fun Date.equalWithinOneSecond(date: Date): Boolean = (this.time - date.time).absoluteValue < 1000L
