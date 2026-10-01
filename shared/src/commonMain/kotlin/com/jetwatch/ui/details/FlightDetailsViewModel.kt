package com.jetwatch.ui.details

import androidx.lifecycle.viewModelScope
import com.jetwatch.ui.JetViewModel
import com.jetwatch.domain.model.FlightDetails
import com.jetwatch.domain.model.LiveTelemetry
import com.jetwatch.domain.model.SavedFlight
import com.jetwatch.domain.repository.FlightRepository
import com.jetwatch.domain.repository.SavedFlightRepository
import com.jetwatch.ui.navigation.DetailsRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailsUiState(
    val loading: Boolean = true,
    val details: FlightDetails? = null,
    val live: LiveTelemetry? = null,
    val name: String? = null,
    val saved: Boolean = false,
    val error: String? = null,
)

class FlightDetailsViewModel(
    request: DetailsRequest,
    private val flights: FlightRepository,
    private val savedFlights: SavedFlightRepository,
) : JetViewModel() {
    private val lookup = request.lookup
    private val live = LiveTelemetry(
        icao24 = request.icao24,
        altitudeMeters = request.altitudeMeters,
        speedMetersPerSecond = request.speedMetersPerSecond,
        headingDegrees = request.headingDegrees,
        onGround = request.onGround,
        originCountry = request.originCountry,
    )

    private val _state = MutableStateFlow(
        DetailsUiState(live = live, name = lookup.ifBlank { null }, loading = lookup.isNotBlank() || live.icao24 != null),
    )
    val state: StateFlow<DetailsUiState> = _state.asStateFlow()

    val followKey: String?
        get() {
            val details = _state.value.details
            return details?.flightNumber ?: lookup.ifBlank { live.icao24 }
        }

    init {
        refresh()
    }

    fun refresh() {
        val query = lookup.ifBlank { live.icao24.orEmpty() }
        if (query.isBlank()) {
            _state.update { it.copy(loading = false) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val result = runCatching { flights.lookup(lookup, live.icao24, preferNetwork = true) }
            val details = result.getOrNull()
            val key = details?.flightNumber ?: query
            _state.update {
                it.copy(
                    loading = false,
                    details = details,
                    saved = savedFlights.isSaved(key),
                    error = result.exceptionOrNull()?.message ?: if (details == null) {
                        "No schedule found for $query."
                    } else {
                        null
                    },
                )
            }
        }
    }

    fun toggleSaved() {
        val details = _state.value.details
        val key = details?.flightNumber ?: lookup.ifBlank { live.icao24 } ?: return
        viewModelScope.launch {
            if (savedFlights.isSaved(key)) {
                savedFlights.remove(key)
                _state.update { it.copy(saved = false) }
            } else {
                savedFlights.save(
                    SavedFlight(
                        key = key,
                        flightNumber = details?.flightNumber,
                        callSign = details?.callSign ?: lookup.ifBlank { null },
                        airlineName = details?.airlineName,
                        status = details?.status,
                        originIata = details?.originIata,
                        originName = details?.originName,
                        destinationIata = details?.destinationIata,
                        destinationName = details?.destinationName,
                        departureLocal = details?.departureLocal,
                        arrivalLocal = details?.arrivalLocal,
                        gate = details?.gate,
                        alertsEnabled = true,
                        savedAtEpochMs = System.currentTimeMillis(),
                    ),
                )
                _state.update { it.copy(saved = true) }
            }
        }
    }
}
