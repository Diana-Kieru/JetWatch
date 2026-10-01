package com.jetwatch.data.remote

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
