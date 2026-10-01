package com.jetwatch.data.remote.aerodatabox

import com.jetwatch.domain.model.AirportSummary
import com.jetwatch.domain.model.FlightDetails
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement

object AeroDataBoxParser {
    private val statusRank = listOf(
        "EnRoute",
        "Approaching",
        "Departed",
        "Boarding",
        "GateClosed",
        "CheckIn",
        "Delayed",
        "Expected",
        "Arrived",
    )

    fun flights(json: Json, payload: String): List<FlightDetails> {
        val root = json.parseToJsonElement(payload)
        return when (root) {
            is JsonArray -> decodeFlights(json, root).mapNotNull { it.toDetails() }
            is JsonObject -> {
                val departures = decodeFlights(json, root["departures"] as? JsonArray)
                    .mapNotNull { it.toDetails(boardRole = "Departs") }
                val arrivals = decodeFlights(json, root["arrivals"] as? JsonArray)
                    .mapNotNull { it.toDetails(boardRole = "Arrives") }
                if (departures.isNotEmpty() || arrivals.isNotEmpty()) {
                    departures + arrivals
                } else {
                    val nested = root["flights"] as? JsonArray ?: root["items"] as? JsonArray
                    decodeFlights(json, nested).mapNotNull { it.toDetails() }
                }
            }
            else -> emptyList()
        }
    }

    fun pickBest(flights: List<FlightDetails>): FlightDetails? =
        flights.minByOrNull { flight ->
            val index = statusRank.indexOf(flight.status)
            if (index < 0) statusRank.size else index
        }

    fun suggestions(json: Json, payload: String): List<String> {
        val root = json.parseToJsonElement(payload)
        val items = when (root) {
            is JsonArray -> root
            is JsonObject -> root["items"] as? JsonArray
            else -> null
        } ?: return emptyList()
        return items.mapNotNull { element ->
            when (element) {
                is JsonPrimitive -> element.contentOrNull
                is JsonObject -> element.string("number") ?: element.string("flightNumber")
                else -> null
            }
        }.distinct()
    }

    fun airports(json: Json, payload: String): List<AirportSummary> {
        val root = json.parseToJsonElement(payload)
        val items = when (root) {
            is JsonArray -> root
            is JsonObject -> root["items"] as? JsonArray
            else -> null
        } ?: return emptyList()
        return items.mapNotNull { element ->
            val obj = element as? JsonObject ?: return@mapNotNull null
            val name = obj.string("name") ?: obj.string("shortName") ?: return@mapNotNull null
            AirportSummary(
                name = name,
                iata = obj.string("iata"),
                icao = obj.string("icao"),
                city = obj.string("municipalityName"),
            )
        }
    }

    private fun decodeFlights(json: Json, array: JsonArray?): List<FlightDto> {
        if (array == null) return emptyList()
        return array.mapNotNull { element ->
            runCatching { json.decodeFromJsonElement(FlightDto.serializer(), element) }.getOrNull()
        }
    }

    private fun JsonObject.string(name: String): String? =
        (this[name] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }
}

private fun FlightDto.toDetails(boardRole: String? = null): FlightDetails? {
    val number = number?.trim()?.ifBlank { null } ?: return null
    val departureTime = departure.bestLocal()
    val arrivalTime = arrival.bestLocal()
    return FlightDetails(
        flightNumber = number.uppercase(),
        callSign = callSign?.trim()?.ifBlank { null },
        airlineName = airline?.name,
        status = status,
        originIata = departure?.airport?.iata,
        originName = departure?.airport.displayName(),
        destinationIata = arrival?.airport?.iata,
        destinationName = arrival?.airport.displayName(),
        departureLocal = departureTime,
        arrivalLocal = arrivalTime,
        gate = departure?.gate ?: arrival?.gate,
        terminal = departure?.terminal ?: arrival?.terminal,
        baggageBelt = arrival?.baggageBelt,
        aircraftModel = aircraft?.model,
        registration = aircraft?.reg,
        boardRole = boardRole,
    )
}

private fun MovementDto?.bestLocal(): String? {
    if (this == null) return null
    return revisedTime?.local
        ?: predictedTime?.local
        ?: scheduledTime?.local
        ?: runwayTime?.local
        ?: revisedTime?.utc
        ?: scheduledTime?.utc
}

private fun AirportDto?.displayName(): String? {
    if (this == null) return null
    return name ?: municipalityName
}
