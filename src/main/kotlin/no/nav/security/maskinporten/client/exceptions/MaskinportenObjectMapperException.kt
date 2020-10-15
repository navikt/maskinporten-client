package no.nav.security.maskinporten.client.exceptions

internal class MaskinportenObjectMapperException(message: String) : Exception("Feil ved deserialisering av response fra maskinporten: $message")