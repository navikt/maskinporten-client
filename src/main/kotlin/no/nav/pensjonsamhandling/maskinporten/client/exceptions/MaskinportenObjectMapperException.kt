package no.nav.pensjonsamhandling.maskinporten.client.exceptions

class MaskinportenObjectMapperException(message: String) : Exception("Feil ved deserialisering av response fra maskinporten: $message")