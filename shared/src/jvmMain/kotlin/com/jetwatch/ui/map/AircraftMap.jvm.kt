package com.jetwatch.ui.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jetwatch.domain.model.Aircraft
import com.jetwatch.domain.model.BoundingBox

@Composable
actual fun AircraftMap(
    aircraft: List<Aircraft>,
    onBoundsChanged: (BoundingBox) -> Unit,
    onAircraftClick: (Aircraft) -> Unit,
    modifier: Modifier,
) {
    LaunchedEffect(Unit) {
        onBoundsChanged(
            BoundingBox(
                south = -5.0,
                west = 33.0,
                north = 2.0,
                east = 42.0,
            ),
        )
    }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Kenya airspace", style = MaterialTheme.typography.titleMedium)
        Text(
            "The live map is on Android. This list uses the same OpenSky feed.",
            style = MaterialTheme.typography.bodyMedium,
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(aircraft, key = { it.icao24 }) { plane ->
                Text(
                    text = plane.callSign?.ifBlank { null } ?: plane.icao24,
                    modifier = Modifier.clickable { onAircraftClick(plane) },
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
