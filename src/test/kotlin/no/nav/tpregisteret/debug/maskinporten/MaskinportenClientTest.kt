package no.nav.tpregisteret.debug.maskinporten

import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import no.nav.tpregisteret.debug.mock.MASKINPORTEN_MOCK_HOST
import no.nav.tpregisteret.debug.mock.MaskinportenMock
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ActiveProfiles("ct")
internal class MaskinportenClientTest {
    private var maskinportenMock: MaskinportenMock = MaskinportenMock()

    @Autowired
    lateinit var maskinportenClient: MaskinportenClient

    @BeforeEach
    internal fun beforeEach() {
        maskinportenMock.reset()
    }

    @AfterAll
    internal fun teardown() {
        maskinportenMock.stop()
    }

    @Test
    fun `reuse token from maskinporten if not expired`() {
        maskinportenMock.`mock valid response for only one call`()

        val firstToken = maskinportenClient.maskinportenToken
        val secondToken = maskinportenClient.maskinportenToken
        assertEquals(firstToken, secondToken)
    }

    @Test
    fun `throws MaskinportenObjectMapperException if response from maskinporten cant be mapped`() {
        maskinportenMock.`mock invalid JSON response`()

        assertThrows<MaskinportenObjectMapperException> { maskinportenClient.maskinportenToken }
    }

    @Test
    fun `Throws MaskinportenClientException when status other than 200 is returned from maskinporten`() {
        maskinportenMock.`mock 500 server error`()

        assertThrows<MaskinportenClientException> { maskinportenClient.maskinportenToken }
    }

    private infix fun String.containWord(word: String) = this.contains(word)
}

internal fun createMaskinportenEnvVariables(privateKey: RSAKey = RSAKeyGenerator(2048).keyID("123").generate()) = mapOf(
        AUDIENCE_ENV_KEY to "testAud",
        ISSUER_ENV_KEY to "testIssuer",
        SCOPE_ENV_KEY to "testScope",
        VALID_IN_SECONDS_ENV_KEY to "120",
        PRIVATE_JWK_ENV_KEY to privateKey.toJSONString(),
        MASKINPORTEN_TOKEN_HOST_ENV_KEY to MASKINPORTEN_MOCK_HOST
)