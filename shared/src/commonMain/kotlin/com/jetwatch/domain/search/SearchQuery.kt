package com.jetwatch.domain.search

sealed interface SearchQuery {
    data class FlightNumber(val number: String) : SearchQuery
    data class Airport(val code: String, val icao: Boolean) : SearchQuery
    data class Airline(val iata: String) : SearchQuery
    data class Text(val term: String) : SearchQuery
}

object SearchQueryParser {
    private val flightNumber = Regex("^[A-Za-z]{2,3}\\s?\\d{1,4}[A-Za-z]?$")
    private val iata = Regex("^[A-Za-z]{3}$")
    private val icao = Regex("^[A-Za-z]{4}$")
    private val airline = Regex("^[A-Za-z]{2}$")

    fun parse(raw: String): SearchQuery {
        val trimmed = raw.trim()
        val compact = trimmed.replace(" ", "")
        return when {
            flightNumber.matches(trimmed) -> SearchQuery.FlightNumber(compact.uppercase())
            icao.matches(trimmed) -> SearchQuery.Airport(trimmed.uppercase(), icao = true)
            iata.matches(trimmed) -> SearchQuery.Airport(trimmed.uppercase(), icao = false)
            airline.matches(trimmed) -> SearchQuery.Airline(trimmed.uppercase())
            else -> SearchQuery.Text(trimmed)
        }
    }

    fun looksLikeFlightNumber(raw: String): Boolean = flightNumber.matches(raw.trim())
}
