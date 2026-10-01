package com.jetwatch.data.repository

import com.jetwatch.data.local.LocalStore
import com.jetwatch.data.remote.OpenSkyClient
import com.jetwatch.data.remote.opensky.OpenSkyParser
import com.jetwatch.domain.model.AircraftSnapshot
import com.jetwatch.domain.model.BoundingBox
import com.jetwatch.domain.repository.AircraftRepository
import kotlinx.coroutines.CancellationException

class AircraftRepositoryImpl(
    private val api: OpenSkyClient,
    private val store: LocalStore,
) : AircraftRepository {
    override suspend fun load(bounds: BoundingBox): AircraftSnapshot {
        return try {
            val response = api.states(bounds)
            when (response.code) {
                429 -> return cached(bounds, "OpenSky rate limit reached. Showing the last positions saved on this phone.")
                !in 200..299 -> return cached(
                    bounds,
                    "OpenSky is unavailable (${response.code}). Showing the last positions saved on this phone.",
                )
            }
            val aircraft = OpenSkyParser.parse(response.body)
            store.replaceAircraft(aircraft)
            AircraftSnapshot(
                aircraft = aircraft.filter { bounds.contains(it.latitude, it.longitude) },
                fromCache = false,
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            cached(bounds, "No network. Showing the last positions saved on this phone.")
        }
    }

    private suspend fun cached(bounds: BoundingBox, message: String): AircraftSnapshot {
        val aircraft = store.aircraftIn(bounds)
        val note = if (aircraft.isEmpty()) {
            "No network, and there are no saved positions for this area yet."
        } else {
            message
        }
        return AircraftSnapshot(aircraft = aircraft, fromCache = true, message = note)
    }
}
