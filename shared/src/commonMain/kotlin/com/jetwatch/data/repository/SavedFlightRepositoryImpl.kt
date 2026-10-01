package com.jetwatch.data.repository

import com.jetwatch.data.local.LocalStore
import com.jetwatch.domain.model.FlightDetails
import com.jetwatch.domain.model.SavedFlight
import com.jetwatch.domain.repository.SavedFlightRepository
import kotlinx.coroutines.flow.Flow

class SavedFlightRepositoryImpl(
    private val store: LocalStore,
) : SavedFlightRepository {
    override fun observe(): Flow<List<SavedFlight>> = store.observeSaved()

    override suspend fun all(): List<SavedFlight> = store.saved()

    override suspend fun isSaved(key: String): Boolean = store.isSaved(key)

    override suspend fun save(flight: SavedFlight) = store.save(flight)

    override suspend fun remove(key: String) = store.remove(key)

    override suspend fun updateFromDetails(key: String, details: FlightDetails) = store.updateSaved(key, details)
}
