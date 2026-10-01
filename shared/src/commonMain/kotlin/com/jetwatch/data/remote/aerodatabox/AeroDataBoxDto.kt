package com.jetwatch.data.remote.aerodatabox

import kotlinx.serialization.Serializable

@Serializable
data class FlightDto(
    val number: String? = null,
    val callSign: String? = null,
    val status: String? = null,
    val airline: AirlineDto? = null,
    val departure: MovementDto? = null,
    val arrival: MovementDto? = null,
    val aircraft: AircraftRefDto? = null,
)

@Serializable
data class AirlineDto(
    val name: String? = null,
    val iata: String? = null,
    val icao: String? = null,
)

@Serializable
data class MovementDto(
    val airport: AirportDto? = null,
    val scheduledTime: TimeDto? = null,
    val revisedTime: TimeDto? = null,
    val predictedTime: TimeDto? = null,
    val runwayTime: TimeDto? = null,
    val gate: String? = null,
    val terminal: String? = null,
    val baggageBelt: String? = null,
)

@Serializable
data class AirportDto(
    val icao: String? = null,
    val iata: String? = null,
    val name: String? = null,
    val municipalityName: String? = null,
)

@Serializable
data class TimeDto(
    val utc: String? = null,
    val local: String? = null,
)

@Serializable
data class AircraftRefDto(
    val reg: String? = null,
    val model: String? = null,
    val modeS: String? = null,
)
