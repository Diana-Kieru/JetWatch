package com.jetwatch

import com.jetwatch.data.remote.aerodatabox.AeroDataBoxParser
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class AeroDataBoxParserTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parsesFlightStatusAndPrefersTheAirborneLeg() {
        val payload = """
            [
              {
                "number": "KQ100",
                "callSign": "KQA100",
                "status": "Arrived",
                "airline": {"name": "Kenya Airways", "iata": "KQ"},
                "departure": {"airport": {"iata": "NBO", "name": "Jomo Kenyatta"}, "scheduledTime": {"utc": "2026-10-01T06:00:00Z", "local": "2026-10-01T09:00+03:00"}},
                "arrival": {"airport": {"iata": "LHR", "name": "Heathrow"}, "scheduledTime": {"utc": "2026-10-01T14:00:00Z", "local": "2026-10-01T15:00+01:00"}, "gate": "B12"}
              },
              {
                "number": "kq100",
                "status": "EnRoute",
                "departure": {"airport": {"iata": "NBO", "name": "Jomo Kenyatta"}, "revisedTime": {"local": "2026-10-01T09:20+03:00"}, "gate": "D4"},
                "arrival": {"airport": {"iata": "LHR", "name": "Heathrow"}}
              }
            ]
        """.trimIndent()

        val flights = AeroDataBoxParser.flights(json, payload)
        val best = AeroDataBoxParser.pickBest(flights)

        assertEquals("KQ100", best?.flightNumber)
        assertEquals("EnRoute", best?.status)
        assertEquals("NBO", best?.originIata)
        assertEquals("LHR", best?.destinationIata)
        assertEquals("D4", best?.gate)
        assertEquals("KQA100", flights.first().callSign)
    }

    @Test
    fun tagsAirportBoardRoles() {
        val payload = """
            {
              "departures": [{"number": "KQ310", "status": "Expected", "departure": {"airport": {"iata": "NBO"}}, "arrival": {"airport": {"iata": "MBA"}}}],
              "arrivals": [{"number": "ET308", "status": "Approaching", "departure": {"airport": {"iata": "ADD"}}, "arrival": {"airport": {"iata": "NBO"}}}]
            }
        """.trimIndent()

        val flights = AeroDataBoxParser.flights(json, payload)

        assertEquals(listOf("Departs", "Arrives"), flights.map { it.boardRole })
    }
}
