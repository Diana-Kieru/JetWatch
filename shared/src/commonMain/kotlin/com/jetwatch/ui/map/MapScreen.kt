package com.jetwatch.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jetwatch.domain.model.Aircraft
import com.jetwatch.ui.format.formatAltitude
import com.jetwatch.ui.format.formatHeading
import com.jetwatch.ui.format.formatSpeed
import com.jetwatch.ui.theme.Amber
import com.jetwatch.ui.theme.Navy

@Composable
fun MapScreen(
    onOpenAircraft: (Aircraft) -> Unit,
    viewModel: MapViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    DisposableEffect(Unit) {
        viewModel.setActive(true)
        onDispose { viewModel.setActive(false) }
    }

    androidx.compose.foundation.layout.Box(modifier.fillMaxSize()) {
        AircraftMap(
            aircraft = state.aircraft,
            onBoundsChanged = viewModel::updateBounds,
            onAircraftClick = viewModel::select,
            modifier = Modifier.fillMaxSize(),
        )
        StatusBanner(
            state = state,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(12.dp),
        )
        state.selected?.let { plane ->
            AircraftCard(
                aircraft = plane,
                onOpen = { onOpenAircraft(plane) },
                onDismiss = viewModel::clearSelection,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp),
            )
        }
    }
}

@Composable
private fun StatusBanner(state: MapUiState, modifier: Modifier = Modifier) {
    val label = when {
        state.loading -> "Updating aircraft"
        state.message != null -> state.message
        state.fromCache -> "Offline · ${state.aircraft.size} last known"
        else -> "Live · ${state.aircraft.size} aircraft"
    }
    ElevatedCard(modifier, shape = RoundedCornerShape(20.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                )
            }
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AircraftCard(
    aircraft: Aircraft,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Amber),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Flight, contentDescription = null, tint = Navy)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        aircraft.callSign ?: "No callsign",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        listOfNotNull(
                            aircraft.originCountry,
                            aircraft.icao24.uppercase(),
                            if (aircraft.onGround) "On the ground" else null,
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LiveStat("Altitude", formatAltitude(aircraft.altitudeMeters), Modifier.weight(1f))
                LiveStat("Speed", formatSpeed(aircraft.speedMetersPerSecond), Modifier.weight(1f))
                LiveStat("Heading", formatHeading(aircraft.headingDegrees), Modifier.weight(1f))
            }
            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Flight details")
            }
        }
    }
}

@Composable
private fun LiveStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
