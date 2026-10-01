package com.jetwatch.domain.alert

enum class AlertKind {
    Delayed,
    Departed,
    Landed,
}

object FlightAlertPolicy {
    private val airborne = setOf("Departed", "EnRoute", "Approaching")

    fun detect(previous: String?, current: String?): AlertKind? {
        if (current.isNullOrBlank() || current == previous) return null
        if (previous == null) return null
        if (current == "Delayed") return AlertKind.Delayed
        if (current == "Arrived") return AlertKind.Landed
        if (current in airborne && previous !in airborne && previous != "Arrived") {
            return AlertKind.Departed
        }
        return null
    }
}
