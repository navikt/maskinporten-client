package no.nav.pensjonsamhandling.maskinporten.client.exceptions

import java.net.http.HttpResponse

class MaskinportenClientException(response: HttpResponse<String>) : Exception("Feil ved henting av token: Status: ${response.statusCode()} , Body: ${response.body()}")