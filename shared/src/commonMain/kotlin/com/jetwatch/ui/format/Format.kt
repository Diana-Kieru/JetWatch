package com.jetwatch.ui.format

import com.jetwatch.domain.model.FlightDetails
import kotlin.math.roundToInt

fun metersToFeet(meters: Double): Int = (meters * 3.28084).roundToInt()

fun metersPerSecondToKnots(metersPerSecond: Double): Int = (metersPerSecond * 1.943844).roundToInt()

fun formatAltitude(meters: Double?): String =
    if (meters == null) "—" else "${metersToFeet(meters)} ft"

fun formatSpeed(metersPerSecond: Double?): String =
    if (metersPerSecond == null) "—" else "${metersPerSecondToKnots(metersPerSecond)} kt"

fun formatHeading(degrees: Double?): String =
    if (degrees == null) "—" else "${degrees.roundToInt()}°"

fun statusLabel(status: String?): String = when (status) {
    null, "" -> "Unknown"
    "EnRoute" -> "En route"
    "GateClosed" -> "Gate closed"
    "Canceled" -> "Cancelled"
    "CanceledUncertain" -> "Possibly cancelled"
    else -> status
}

fun formatClock(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    val normalized = raw.trim().replace(" ", "T")
    val clock = Regex("""T(\d{2}:\d{2})""").find(normalized)?.groupValues?.get(1)
    return clock ?: raw
}

fun routeLabel(origin: String?, destination: String?): String {
    val from = origin?.ifBlank { null } ?: "—"
    val to = destination?.ifBlank { null } ?: "—"
    return "$from → $to"
}

fun FlightDetails.route(): String = routeLabel(
    originIata ?: originName,
    destinationIata ?: destinationName,
)

fun airportLabel(code: String?, name: String?): String = when {
    !code.isNullOrBlank() && !name.isNullOrBlank() -> "$code · $name"
    !code.isNullOrBlank() -> code
    !name.isNullOrBlank() -> name
    else -> "—"
}
