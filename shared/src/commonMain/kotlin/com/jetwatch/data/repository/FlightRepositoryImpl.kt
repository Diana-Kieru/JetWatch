package com.jetwatch.data.repository

import com.jetwatch.data.local.LocalStore
import com.jetwatch.data.remote.AeroDataBoxClient
import com.jetwatch.data.remote.aerodatabox.AeroDataBoxParser
import com.jetwatch.domain.model.FlightDetails
import com.jetwatch.domain.model.SearchResults
import com.jetwatch.domain.repository.FlightRepository
import com.jetwatch.domain.search.SearchQuery
import com.jetwatch.domain.search.SearchQueryParser
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.Json

class FlightRepositoryImpl(
    private val api: AeroDataBoxClient,
    private val store: LocalStore,
    private val json: Json,
) : FlightRepository {
    override suspend fun search(query: SearchQuery): SearchResults {
        return try {
            when (query) {
                is SearchQuery.FlightNumber -> SearchResults(flights = flightsByNumber(query.number))
                is SearchQuery.Airport -> SearchResults(flights = airportBoard(query.code, query.icao))
                is SearchQuery.Airline -> {
                    val flights = runCatching { airlineFlights(query.iata) }.getOrDefault(emptyList())
                    SearchResults(
                        flights = flights,
                        suggestions = if (flights.isEmpty()) suggestions(query.iata) else emptyList(),
                    )
                }
                is SearchQuery.Text -> {
                    if (query.term.length < 2) error("Type at least 2 characters.")
                    SearchResults(
                        airports = airports(query.term),
                        suggestions = suggestions(query.term),
                    )
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            if (query is SearchQuery.FlightNumber) {
                store.cachedFlight(query.number)?.let { return SearchResults(flights = listOf(it)) }
            }
            throw error
        }
    }

    override suspend fun lookup(query: String, icao24: String?, preferNetwork: Boolean): FlightDetails? {
        val trimmed = query.trim()
        val modeS = icao24?.trim()?.lowercase()?.ifBlank { null }
        if (trimmed.isEmpty() && modeS == null) return null
        if (!preferNetwork) {
            store.cachedFlight(trimmed)?.let { return it }
            modeS?.let { store.cachedFlight(it)?.let { flight -> return flight } }
        }
        return try {
            val flights = when {
                trimmed.isNotEmpty() && SearchQueryParser.looksLikeFlightNumber(trimmed) ->
                    flightsByNumber(trimmed).ifEmpty { flightsByCallSign(trimmed) }
                trimmed.isNotEmpty() ->
                    flightsByCallSign(trimmed).ifEmpty { flightsByNumber(trimmed) }
                modeS != null -> status("icao24", modeS)
                else -> emptyList()
            }.ifEmpty { modeS?.let { status("icao24", it) } ?: emptyList() }
            val best = AeroDataBoxParser.pickBest(flights)
            if (best != null) store.cacheFlight(best, modeS)
            best ?: store.cachedFlight(trimmed) ?: modeS?.let { store.cachedFlight(it) }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: IllegalStateException) {
            store.cachedFlight(trimmed) ?: modeS?.let { store.cachedFlight(it) } ?: throw error
        } catch (_: Exception) {
            store.cachedFlight(trimmed) ?: modeS?.let { store.cachedFlight(it) }
        }
    }

    private suspend fun flightsByNumber(number: String): List<FlightDetails> =
        status("number", number.replace(" ", "").uppercase())

    private suspend fun flightsByCallSign(callSign: String): List<FlightDetails> =
        status("callsign", callSign.replace(" ", "").uppercase())

    private suspend fun status(searchBy: String, value: String): List<FlightDetails> {
        val payload = api.flightStatus(searchBy, value, today())
        val flights = AeroDataBoxParser.flights(json, payload)
        flights.firstOrNull()?.let { store.cacheFlight(it) }
        return flights
    }

    private suspend fun airportBoard(code: String, icao: Boolean): List<FlightDetails> {
        val today = today()
        val payload = api.airportBoard(
            codeType = if (icao) "icao" else "iata",
            code = code,
            from = "${today}T00:00",
            to = "${today}T23:59",
        )
        return AeroDataBoxParser.flights(json, payload).take(40)
    }

    private suspend fun airlineFlights(code: String): List<FlightDetails> {
        val payload = api.airlineFlights(code, today())
        return AeroDataBoxParser.flights(json, payload).take(40)
    }

    private suspend fun suggestions(term: String): List<String> =
        AeroDataBoxParser.suggestions(json, api.searchFlightNumbers(term))

    private suspend fun airports(term: String) =
        AeroDataBoxParser.airports(json, api.searchAirports(term))

    private fun today(): String = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
}
