package com.jetwatch.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Aircraft(
    val icao24: String,
    val callSign: String?,
    val originCountry: String?,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double?,
    val onGround: Boolean,
    val speedMetersPerSecond: Double?,
    val headingDegrees: Double?,
)
