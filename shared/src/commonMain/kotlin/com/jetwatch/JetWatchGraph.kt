package com.jetwatch

import com.jetwatch.data.local.LocalStore
import com.jetwatch.data.remote.AeroDataBoxClient
import com.jetwatch.data.remote.OpenSkyClient
import com.jetwatch.data.remote.OpenSkyTokenProvider
import com.jetwatch.data.remote.createHttpClient
import com.jetwatch.data.repository.AircraftRepositoryImpl
import com.jetwatch.data.repository.FlightRepositoryImpl
import com.jetwatch.data.repository.SavedFlightRepositoryImpl
import com.jetwatch.domain.repository.AircraftRepository
import com.jetwatch.domain.repository.FlightRepository
import com.jetwatch.domain.repository.SavedFlightRepository
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath

data class JetWatchConfig(
    val aeroDataBoxKey: String,
    val openSkyClientId: String,
    val openSkyClientSecret: String,
)

class JetWatchGraph(
    config: JetWatchConfig,
    storeFile: String,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }
    private val http = createHttpClient()
    private val store = LocalStore(storeFile.toPath(), json)

    val aircraft: AircraftRepository = AircraftRepositoryImpl(
        api = OpenSkyClient(
            http = http,
            tokens = OpenSkyTokenProvider(
                http = http,
                json = json,
                clientId = config.openSkyClientId,
                clientSecret = config.openSkyClientSecret,
            ),
        ),
        store = store,
    )
    val flights: FlightRepository = FlightRepositoryImpl(
        api = AeroDataBoxClient(http, config.aeroDataBoxKey),
        store = store,
        json = json,
    )
    val saved: SavedFlightRepository = SavedFlightRepositoryImpl(store)
}
