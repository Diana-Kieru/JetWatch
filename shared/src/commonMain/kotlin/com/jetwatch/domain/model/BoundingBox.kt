package com.jetwatch.domain.model

data class BoundingBox(
    val south: Double,
    val west: Double,
    val north: Double,
    val east: Double,
) {
    val latSpan: Double get() = north - south

    val lonSpan: Double
        get() = if (east >= west) east - west else (180 - west) + (east + 180)

    fun isTooWide(maxSpanDegrees: Double = 30.0): Boolean =
        latSpan > maxSpanDegrees || lonSpan > maxSpanDegrees

    fun contains(latitude: Double, longitude: Double): Boolean {
        if (latitude !in south..north) return false
        return if (west <= east) longitude in west..east else longitude >= west || longitude <= east
    }
}
