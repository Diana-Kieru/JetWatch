package com.jetwatch

import com.jetwatch.data.remote.opensky.OpenSkyParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OpenSkyParserTest {
    @Test
    fun parsesStateVectorsAndSkipsRowsWithoutAPosition() {
        val payload = """
            {
              "time": 1720000000,
              "states": [
                ["abc123", "KQ100  ", "Kenya", 1720000000, 1720000001, 36.8, -1.3, 11000, false, 230.0, 90.0, 0, null, 11200, "1234", false, 0],
                ["nogps", "BA1", "United Kingdom", null, null, null, null, null, false, null, null, null, null, null, null, false, 0],
                ["grounded", "5Y   ", "Kenya", 1720000000, 1720000001, 36.9, -1.32, null, true, 0, null, null, null, null, null, false, 0]
              ]
            }
        """.trimIndent()

        val aircraft = OpenSkyParser.parse(payload)

        assertEquals(2, aircraft.size)
        val kenya = aircraft.first()
        assertEquals("abc123", kenya.icao24)
        assertEquals("KQ100", kenya.callSign)
        assertEquals(36.8, kenya.longitude, 0.001)
        assertEquals(-1.3, kenya.latitude, 0.001)
        assertEquals(11000.0, kenya.altitudeMeters!!, 0.001)
        assertEquals(90.0, kenya.headingDegrees!!, 0.001)
        assertEquals(false, kenya.onGround)
        assertNull(aircraft[1].headingDegrees)
        assertTrue(aircraft[1].onGround)
    }

    @Test
    fun emptyStatesAreAnEmptyList() {
        assertTrue(OpenSkyParser.parse("""{"time":1,"states":null}""").isEmpty())
    }
}
