package no.nav.security.maskinporten.client

import no.nav.security.maskinporten.client.exceptions.MaskinportenClientException
import no.nav.security.maskinporten.client.exceptions.MaskinportenObjectMapperException
import no.nav.security.maskinporten.client.mock.MaskinportenMock
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MaskinportenClientTest {
    private var maskinportenMock: MaskinportenMock = MaskinportenMock()

    private lateinit var maskinportenClient: MaskinportenClient

    @BeforeEach
    internal fun beforeEach() {
        maskinportenMock.reset()
        maskinportenClient = MaskinportenClient(MaskinportenMock.createMaskinportenConfig())
    }

    @AfterAll
    internal fun teardown() {
        maskinportenMock.stop()
    }

    @Test
    fun `reuse token from maskinporten if not expired`() {
        maskinportenMock.`mock valid response for only one call`()

        val firstToken = maskinportenClient.maskinportenTokenString
        val secondToken = maskinportenClient.maskinportenTokenString
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
}