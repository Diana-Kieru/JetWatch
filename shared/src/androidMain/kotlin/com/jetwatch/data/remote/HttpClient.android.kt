package com.jetwatch.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout

actual fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    install(HttpTimeout) {
        connectTimeoutMillis = 15_000
        requestTimeoutMillis = 25_000
    }
}
