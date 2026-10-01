package com.jetwatch.data.local

import com.jetwatch.domain.model.Aircraft
import com.jetwatch.domain.model.BoundingBox
import com.jetwatch.domain.model.FlightDetails
import com.jetwatch.domain.model.SavedFlight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path

@Serializable
private data class DiskStore(
    val aircraft: List<Aircraft> = emptyList(),
    val saved: List<SavedFlight> = emptyList(),
    val flights: Map<String, FlightDetails> = emptyMap(),
)

class LocalStore(
    private val path: Path,
    private val json: Json,
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
) {
    private val mutex = Mutex()
    private var memory = DiskStore()
    private var loaded = false

    suspend fun replaceAircraft(items: List<Aircraft>) = update { it.copy(aircraft = items) }

    suspend fun aircraftIn(bounds: BoundingBox): List<Aircraft> =
        read().aircraft.filter { bounds.contains(it.latitude, it.longitude) }

    fun observeSaved(): Flow<List<SavedFlight>> = flow {
        emit(read().saved)
        emitAll(updates.map { it.saved })
    }

    private val updates = MutableSharedFlow<DiskStore>(replay = 1)

    suspend fun saved(): List<SavedFlight> = read().saved

    suspend fun isSaved(key: String): Boolean = read().saved.any { it.key == key }

    suspend fun save(flight: SavedFlight) = update { store ->
        store.copy(saved = listOf(flight) + store.saved.filterNot { it.key == flight.key })
    }

    suspend fun remove(key: String) = update { store ->
        store.copy(saved = store.saved.filterNot { it.key == key })
    }

    suspend fun updateSaved(key: String, details: FlightDetails) = update { store ->
        store.copy(
            saved = store.saved.map { flight ->
                if (flight.key != key) flight else flight.copy(
                    flightNumber = details.flightNumber,
                    callSign = details.callSign ?: flight.callSign,
                    airlineName = details.airlineName ?: flight.airlineName,
                    status = details.status ?: flight.status,
                    originIata = details.originIata ?: flight.originIata,
                    originName = details.originName ?: flight.originName,
                    destinationIata = details.destinationIata ?: flight.destinationIata,
                    destinationName = details.destinationName ?: flight.destinationName,
                    departureLocal = details.departureLocal ?: flight.departureLocal,
                    arrivalLocal = details.arrivalLocal ?: flight.arrivalLocal,
                    gate = details.gate ?: flight.gate,
                )
            },
        )
    }

    suspend fun cachedFlight(key: String): FlightDetails? {
        val normalized = key.replace(" ", "").uppercase()
        val store = read()
        return store.flights[normalized] ?: store.flights[key]
    }

    suspend fun cacheFlight(details: FlightDetails, extraKey: String? = null) = update { store ->
        val next = store.flights.toMutableMap()
        next[details.flightNumber.uppercase()] = details
        details.callSign?.let { next[it.uppercase()] = details }
        extraKey?.let { next[it.uppercase()] = details }
        store.copy(flights = next)
    }

    private suspend fun read(): DiskStore {
        ensure()
        return memory
    }

    private suspend fun update(block: (DiskStore) -> DiskStore) {
        ensure()
        mutex.withLock {
            memory = block(memory)
            write(memory)
            updates.emit(memory)
        }
    }

    private suspend fun ensure() {
        if (loaded) return
        mutex.withLock {
            if (loaded) return
            memory = if (fileSystem.exists(path)) {
                runCatching {
                    json.decodeFromString(DiskStore.serializer(), fileSystem.read(path) { readUtf8() })
                }.getOrDefault(DiskStore())
            } else {
                DiskStore()
            }
            loaded = true
            updates.emit(memory)
        }
    }

    private fun write(store: DiskStore) {
        path.parent?.let { parent -> fileSystem.createDirectories(parent) }
        fileSystem.write(path) {
            writeUtf8(json.encodeToString(DiskStore.serializer(), store))
        }
    }
}
