package com.jetwatch.ui.saved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.jetwatch.ui.BackTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jetwatch.domain.model.SavedFlight
import com.jetwatch.ui.format.formatClock
import com.jetwatch.ui.format.routeLabel
import com.jetwatch.ui.format.statusLabel

@Composable
fun SavedFlightsScreen(
    onBack: () -> Unit,
    onOpenFlight: (String) -> Unit,
    viewModel: SavedFlightsViewModel,
    modifier: Modifier = Modifier,
) {
    val flights by viewModel.flights.collectAsState()
    Scaffold(
        modifier = modifier,
        topBar = { BackTopBar("Saved", onBack) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
    Column(
        Modifier.fillMaxSize().padding(padding).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (flights.isEmpty()) {
            Text("Follow a flight from its details screen. JetWatch checks it in the background and notifies you when it is delayed, departs, or lands.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(flights, key = { it.key }) { flight ->
                    SavedRow(
                        flight = flight,
                        onOpen = { onOpenFlight(flight.flightNumber ?: flight.key) },
                        onRemove = { viewModel.remove(flight.key) },
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun SavedRow(flight: SavedFlight, onOpen: () -> Unit, onRemove: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(flight.flightNumber ?: flight.key, style = MaterialTheme.typography.titleMedium)
            flight.airlineName?.let { Text(it) }
            Text(routeLabel(flight.originIata ?: flight.originName, flight.destinationIata ?: flight.destinationName))
            Text("${statusLabel(flight.status)} · ${formatClock(flight.departureLocal)}")
            Row {
                TextButton(onClick = onRemove) { Text("Unfollow") }
            }
        }
    }
}
