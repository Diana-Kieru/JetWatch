package com.jetwatch

import com.jetwatch.domain.model.BoundingBox
import com.jetwatch.domain.search.SearchQuery
import com.jetwatch.domain.search.SearchQueryParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SearchQueryParserTest {
    @Test
    fun classifiesFlightAirportAndAirline() {
        assertEquals(SearchQuery.FlightNumber("KQ100"), SearchQueryParser.parse("kq 100"))
        assertEquals(SearchQuery.Airport("NBO", icao = false), SearchQueryParser.parse("nbo"))
        assertEquals(SearchQuery.Airport("HKJK", icao = true), SearchQueryParser.parse("HKJK"))
        assertEquals(SearchQuery.Airline("KQ"), SearchQueryParser.parse("kq"))
        assertEquals(SearchQuery.Text("Nairobi"), SearchQueryParser.parse("Nairobi"))
    }
}

class FlightAlertPolicyTest {
    @Test
    fun notifiesOnDelayDepartureAndLandingOnlyWhenStatusChanges() {
        assertEquals(null, com.jetwatch.domain.alert.FlightAlertPolicy.detect(null, "Delayed"))
        assertEquals(
            com.jetwatch.domain.alert.AlertKind.Delayed,
            com.jetwatch.domain.alert.FlightAlertPolicy.detect("Expected", "Delayed"),
        )
        assertEquals(
            com.jetwatch.domain.alert.AlertKind.Departed,
            com.jetwatch.domain.alert.FlightAlertPolicy.detect("Boarding", "EnRoute"),
        )
        assertEquals(
            null,
            com.jetwatch.domain.alert.FlightAlertPolicy.detect("EnRoute", "Approaching"),
        )
        assertEquals(
            com.jetwatch.domain.alert.AlertKind.Landed,
            com.jetwatch.domain.alert.FlightAlertPolicy.detect("Approaching", "Arrived"),
        )
    }
}

class BoundingBoxTest {
    @Test
    fun containsPointsAndRejectsAWorldView() {
        val kenya = BoundingBox(south = -5.0, west = 33.0, north = 5.0, east = 42.0)
        assertTrue(kenya.contains(-1.3, 36.8))
        assertFalse(kenya.contains(10.0, 36.8))
        assertTrue(kenya.isTooWide(maxSpanDegrees = 8.0))
        assertFalse(kenya.isTooWide(maxSpanDegrees = 30.0))
    }
}
