package com.jetwatch.domain.model

data class AircraftSnapshot(
    val aircraft: List<Aircraft>,
    val fromCache: Boolean,
    val message: String? = null,
)
