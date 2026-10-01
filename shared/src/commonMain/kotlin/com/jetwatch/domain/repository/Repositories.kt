package com.jetwatch.domain.repository

import com.jetwatch.domain.model.AircraftSnapshot
import com.jetwatch.domain.model.BoundingBox
import com.jetwatch.domain.model.FlightDetails
import com.jetwatch.domain.model.SavedFlight
import com.jetwatch.domain.model.SearchResults
import com.jetwatch.domain.search.SearchQuery
import kotlinx.coroutines.flow.Flow

interface AircraftRepository {
    suspend fun load(bounds: BoundingBox): AircraftSnapshot
}

interface FlightRepository {
    suspend fun search(query: SearchQuery): SearchResults

    suspend fun lookup(query: String, icao24: String? = null, preferNetwork: Boolean = false): FlightDetails?
}

interface SavedFlightRepository {
    fun observe(): Flow<List<SavedFlight>>

    suspend fun all(): List<SavedFlight>

    suspend fun isSaved(key: String): Boolean

    suspend fun save(flight: SavedFlight)

    suspend fun remove(key: String)

    suspend fun updateFromDetails(key: String, details: FlightDetails)
}
