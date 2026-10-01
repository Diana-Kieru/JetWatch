package com.jetwatch.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jetwatch.domain.model.AirportSummary
import com.jetwatch.domain.model.FlightDetails
import com.jetwatch.ui.format.formatClock
import com.jetwatch.ui.format.routeLabel
import com.jetwatch.ui.format.statusLabel

@Composable
fun SearchScreen(
    onOpenFlight: (String) -> Unit,
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Search", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Flight, airport, or airline") },
            placeholder = { Text("KQ100, NBO, or Nairobi") },
            singleLine = true,
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onSearch = { viewModel.search() },
            ),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = androidx.compose.ui.text.input.ImeAction.Search,
            ),
        )
        Text(
            "Flight numbers, a 3-letter airport, a 2-letter airline, or an airport name.",
            style = MaterialTheme.typography.bodySmall,
        )
        if (state.loading) CircularProgressIndicator()
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.results.airports, key = { it.iata ?: it.icao ?: it.name }) { airport ->
                AirportRow(airport) {
                    val code = airport.iata ?: airport.icao ?: return@AirportRow
                    viewModel.search(code)
                }
            }
            items(state.results.suggestions, key = { "suggestion-$it" }) { number ->
                SuggestionRow(number) { viewModel.search(number) }
            }
            items(state.results.flights, key = { flightKey(it) }) { flight ->
                FlightRow(flight) { onOpenFlight(flight.flightNumber) }
            }
            if (state.searched && !state.loading && state.error == null &&
                state.results.flights.isEmpty() && state.results.airports.isEmpty() && state.results.suggestions.isEmpty()
            ) {
                item { Text("Nothing matched that search.") }
            }
        }
    }
}

@Composable
private fun FlightRow(flight: FlightDetails, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(flight.flightNumber, style = MaterialTheme.typography.titleMedium)
            Text(routeLabel(flight.originIata ?: flight.originName, flight.destinationIata ?: flight.destinationName))
            Text("${flight.boardRole?.let { "$it · " }.orEmpty()}${statusLabel(flight.status)} · ${formatClock(flight.departureLocal)}")
        }
    }
}

@Composable
private fun AirportRow(airport: AirportSummary, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(12.dp)) {
            Text(airport.name, style = MaterialTheme.typography.titleMedium)
            Text(listOfNotNull(airport.iata, airport.icao, airport.city).joinToString(" · "))
        }
    }
}

@Composable
private fun SuggestionRow(number: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Text(number, Modifier.padding(12.dp), style = MaterialTheme.typography.titleMedium)
    }
}

private fun flightKey(flight: FlightDetails): String =
    listOf(flight.boardRole, flight.flightNumber, flight.departureLocal, flight.originIata).joinToString("-")
