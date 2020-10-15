package no.nav.security.maskinporten.client

import java.util.*

internal infix fun Date.addSeconds(seconds: Int): Date = Date(time + seconds * ONE_SECOND_IN_MILLISECONDS)
internal const val ONE_SECOND_IN_MILLISECONDS = 1000