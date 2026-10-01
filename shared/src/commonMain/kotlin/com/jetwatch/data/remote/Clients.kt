package com.jetwatch.data.remote

import com.jetwatch.domain.model.BoundingBox
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import io.ktor.http.takeFrom
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.datetime.Clock
import kotlinx.serialization.json.longOrNull

class OpenSkyTokenProvider(
    private val http: HttpClient,
    private val json: Json,
    private val clientId: String,
    private val clientSecret: String,
) {
    private var token: String? = null
    private var expiryEpochMs: Long = 0

    suspend fun authorizationHeader(): String? {
        if (clientId.isBlank() || clientSecret.isBlank()) return null
        val now = Clock.System.now().toEpochMilliseconds()
        val current = token
        if (current != null && now < expiryEpochMs) return "Bearer $current"
        val fetched = fetch(now) ?: return null
        return "Bearer $fetched"
    }

    private suspend fun fetch(now: Long): String? = runCatching {
        val response = http.post(TOKEN_URL) {
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("grant_type", "client_credentials")
                        append("client_id", clientId)
                        append("client_secret", clientSecret)
                    },
                ),
            )
        }
        if (!response.status.isSuccess()) return null
        val obj = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val access = obj["access_token"]?.jsonPrimitive?.content ?: return null
        val expiresIn = obj["expires_in"]?.jsonPrimitive?.longOrNull ?: 1_800
        token = access
        expiryEpochMs = now + (expiresIn - 60).coerceAtLeast(30) * 1_000
        access
    }.getOrNull()

    private companion object {
        const val TOKEN_URL =
            "https://auth.opensky-network.org/auth/realms/opensky-network/protocol/openid-connect/token"
    }
}

class OpenSkyClient(
    private val http: HttpClient,
    private val tokens: OpenSkyTokenProvider,
) {
    suspend fun states(bounds: BoundingBox): RemoteText {
        val response = http.get("https://opensky-network.org/api/states/all") {
            url {
                parameters.append("lamin", bounds.south.toString())
                parameters.append("lomin", bounds.west.toString())
                parameters.append("lamax", bounds.north.toString())
                parameters.append("lomax", bounds.east.toString())
            }
            header("User-Agent", "JetWatch/1.0 (flight tracker)")
            tokens.authorizationHeader()?.let { header("Authorization", it) }
        }
        return RemoteText(response.status.value, response.bodyAsText())
    }
}

class AeroDataBoxClient(
    private val http: HttpClient,
    private val apiKey: String,
) {
    suspend fun flightStatus(searchBy: String, value: String, date: String): String =
        get("flights/$searchBy/$value/$date", mapOf("dateLocalRole" to "Both"))

    suspend fun airportBoard(codeType: String, code: String, from: String, to: String): String =
        get("flights/airports/$codeType/$code/$from/$to")

    suspend fun airlineFlights(code: String, date: String): String =
        get("flights/airlines/iata/$code/$date")

    suspend fun searchFlightNumbers(query: String): String =
        get("flights/search/term", mapOf("q" to query, "limit" to "10"))

    suspend fun searchAirports(query: String): String =
        get("airports/search/term", mapOf("q" to query, "limit" to "8"))

    private suspend fun get(path: String, query: Map<String, String> = emptyMap()): String {
        if (apiKey.isBlank()) {
            error("Add AERODATABOX_API_KEY to local.properties, then rebuild. Live aircraft on the map still work without it.")
        }
        val response = http.get {
            url {
                takeFrom("https://aerodatabox.p.rapidapi.com/")
                path.split("/").filter { it.isNotEmpty() }.forEach { appendPathSegments(it) }
                query.forEach { (key, value) -> parameters.append(key, value) }
            }
            header("X-RapidAPI-Key", apiKey)
            header("X-RapidAPI-Host", "aerodatabox.p.rapidapi.com")
            header("Accept", "application/json")
        }
        return when (response.status.value) {
            204, 404 -> "[]"
            in 200..299 -> response.bodyAsText().ifBlank { "[]" }
            401, 403 -> error("AeroDataBox rejected the API key in local.properties.")
            429 -> error("AeroDataBox rate limit reached. Try again later, or open a flight that is already saved on this phone.")
            else -> error("AeroDataBox returned ${response.status.value}.")
        }
    }
}

data class RemoteText(val code: Int, val body: String)
