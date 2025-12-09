package no.nav.pensjonsamhandling.maskinporten.client

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.SignedJWT
import no.nav.pensjonsamhandling.maskinporten.client.MaskinportenGrantTokenGenerator.Companion.SCOPE_CLAIM
import no.nav.pensjonsamhandling.maskinporten.client.mock.MaskinportenMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.math.absoluteValue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MaskinportenGrantTokenGeneratorTest {
    private val privateKey: RSAKey = RSAKeyGenerator(2048).keyID("123").generate()
    private val publicKey: RSAKey = privateKey.toPublicJWK()
    private val scopes = listOf("testScope1", "testScope2")

    @Test
    fun `Token is signed with private key in environment variables`() {
        val config = MaskinportenMock.createMaskinportenConfig(privateKey)
        val generator = MaskinportenGrantTokenGenerator(config)
        val signedJWT = SignedJWT.parse(generator.generateJWT(scopes))
        val verifier: JWSVerifier = RSASSAVerifier(publicKey)

        assertTrue(signedJWT.verify(verifier))
    }

    @Test
    fun `Algorithm in token header is rsa256`() {
        val config = MaskinportenMock.createMaskinportenConfig(privateKey)
        val generator = MaskinportenGrantTokenGenerator(config)
        val signedJWT = SignedJWT.parse(generator.generateJWT(scopes))

        assertEquals("RS256", (signedJWT.header.algorithm as JWSAlgorithm).name)
    }

    @Test
    fun `Required claims added to token body`() {
        val config = MaskinportenMock.createMaskinportenConfig(privateKey)
        val generator = MaskinportenGrantTokenGenerator(config)
        val signedJWT = SignedJWT.parse(generator.generateJWT(scopes))

        assertEquals(config.issuer, signedJWT.jwtClaimsSet.audience[0])
        assertEquals(config.clientId, signedJWT.jwtClaimsSet.issuer)
        assertEquals(scopes, signedJWT.jwtClaimsSet.getStringClaim(SCOPE_CLAIM).split(' '))
    }

    @Test
    fun `Required timestamps are added to token body`() {
        val config = MaskinportenMock.createMaskinportenConfig(privateKey)
        val generator = MaskinportenGrantTokenGenerator(config)
        val signedJWT = SignedJWT.parse(generator.generateJWT(scopes))

        val issuedAt = signedJWT.jwtClaimsSet.issueTime
        val expirationTime = signedJWT.jwtClaimsSet.expirationTime

        assertTrue(Date() equalWithinOneSecond issuedAt)
        assertTrue((Date() addSeconds config.expiresInSeconds) equalWithinOneSecond expirationTime)
    }

    private infix fun Date.equalWithinOneSecond(date: Date): Boolean = (time - date.time).absoluteValue < 1000L

}
