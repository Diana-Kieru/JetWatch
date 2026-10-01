package com.jetwatch.ui.navigation

import com.jetwatch.domain.model.Aircraft

data class DetailsRequest(
    val lookup: String = "",
    val icao24: String? = null,
    val altitudeMeters: Double? = null,
    val speedMetersPerSecond: Double? = null,
    val headingDegrees: Double? = null,
    val onGround: Boolean? = null,
    val originCountry: String? = null,
) {
    companion object {
        fun from(aircraft: Aircraft) = DetailsRequest(
            lookup = aircraft.callSign.orEmpty(),
            icao24 = aircraft.icao24,
            altitudeMeters = aircraft.altitudeMeters,
            speedMetersPerSecond = aircraft.speedMetersPerSecond,
            headingDegrees = aircraft.headingDegrees,
            onGround = aircraft.onGround,
            originCountry = aircraft.originCountry,
        )
    }
}
