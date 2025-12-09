package no.nav.pensjonsamhandling.maskinporten.client

import java.util.*

internal fun String.suffix(s: String) = if(endsWith(s)) this else plus(s)
internal infix fun Date.addSeconds(seconds: Long): Date = Date.from(toInstant().plusSeconds(seconds))
