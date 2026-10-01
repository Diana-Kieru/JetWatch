package com.jetwatch.data.remote.opensky

import com.jetwatch.domain.model.Aircraft
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

object OpenSkyParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(payload: String): List<Aircraft> {
        val root = json.parseToJsonElement(payload).jsonObject
        val states = root["states"] ?: return emptyList()
        if (states is JsonNull) return emptyList()
        return states.jsonArray.mapNotNull(::aircraft)
    }

    private fun aircraft(row: kotlinx.serialization.json.JsonElement): Aircraft? {
        val values = row as? JsonArray ?: return null
        val icao24 = values.stringOrNull(0) ?: return null
        val latitude = values.doubleOrNull(6) ?: return null
        val longitude = values.doubleOrNull(5) ?: return null
        return Aircraft(
            icao24 = icao24,
            callSign = values.stringOrNull(1)?.trim()?.ifBlank { null },
            originCountry = values.stringOrNull(2),
            longitude = longitude,
            latitude = latitude,
            altitudeMeters = values.doubleOrNull(7),
            onGround = values.boolOrNull(8) ?: false,
            speedMetersPerSecond = values.doubleOrNull(9),
            headingDegrees = values.doubleOrNull(10),
        )
    }

    private fun JsonArray.value(index: Int) = getOrNull(index)?.takeUnless { it is JsonNull }

    private fun JsonArray.stringOrNull(index: Int) = (value(index) as? JsonPrimitive)?.contentOrNull

    private fun JsonArray.doubleOrNull(index: Int) = (value(index) as? JsonPrimitive)?.doubleOrNull

    private fun JsonArray.boolOrNull(index: Int) = (value(index) as? JsonPrimitive)?.booleanOrNull
}
