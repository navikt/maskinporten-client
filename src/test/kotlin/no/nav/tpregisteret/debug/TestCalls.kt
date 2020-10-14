package no.nav.tpregisteret.debug

import no.nav.tpregisteret.debug.maskinporten.MaskinportenClient
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("it")
class TestCalls {
    @Autowired
    lateinit var maskinportenClient: MaskinportenClient
    @Autowired
    lateinit var restTemplateBuilder: RestTemplateBuilder

    lateinit var restTemplate: RestTemplate

    @BeforeAll
    fun init(){
        restTemplate = restTemplateBuilder.rootUri("https://tpregisteret.nais.preprod.local").build()
    }

    @Test
    fun `Call person forhold`(){
        restTemplate.exchange<Any>(
            "/person/forhold",
            HttpMethod.GET,
            HttpEntity(
                null,
                HttpHeaders().apply {
                    setBearerAuth(maskinportenClient.maskinportenToken)
                    set("fnr", "00000000000")
                }
            )
        ).apply {
            print(statusCode)
        }
    }
}