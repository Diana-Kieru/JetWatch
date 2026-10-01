package com.jetwatch.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FlightDetails(
    val flightNumber: String,
    val callSign: String? = null,
    val airlineName: String? = null,
    val status: String? = null,
    val originIata: String? = null,
    val originName: String? = null,
    val destinationIata: String? = null,
    val destinationName: String? = null,
    val departureLocal: String? = null,
    val arrivalLocal: String? = null,
    val gate: String? = null,
    val terminal: String? = null,
    val baggageBelt: String? = null,
    val aircraftModel: String? = null,
    val registration: String? = null,
    val boardRole: String? = null,
)

@Serializable
data class AirportSummary(
    val name: String,
    val iata: String? = null,
    val icao: String? = null,
    val city: String? = null,
)

data class SearchResults(
    val flights: List<FlightDetails> = emptyList(),
    val airports: List<AirportSummary> = emptyList(),
    val suggestions: List<String> = emptyList(),
)

data class LiveTelemetry(
    val icao24: String? = null,
    val altitudeMeters: Double? = null,
    val speedMetersPerSecond: Double? = null,
    val headingDegrees: Double? = null,
    val onGround: Boolean? = null,
    val originCountry: String? = null,
)

@Serializable
data class SavedFlight(
    val key: String,
    val flightNumber: String?,
    val callSign: String?,
    val airlineName: String?,
    val status: String?,
    val originIata: String?,
    val originName: String?,
    val destinationIata: String?,
    val destinationName: String?,
    val departureLocal: String?,
    val arrivalLocal: String?,
    val gate: String?,
    val alertsEnabled: Boolean,
    val savedAtEpochMs: Long,
)
