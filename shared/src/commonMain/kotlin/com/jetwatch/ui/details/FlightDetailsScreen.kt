package com.jetwatch.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jetwatch.domain.model.FlightDetails
import com.jetwatch.domain.model.LiveTelemetry
import com.jetwatch.ui.format.formatAltitude
import com.jetwatch.ui.format.formatClock
import com.jetwatch.ui.format.formatHeading
import com.jetwatch.ui.format.formatSpeed
import com.jetwatch.ui.format.statusLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightDetailsScreen(
    onBack: () -> Unit,
    onFollow: () -> Unit,
    viewModel: FlightDetailsViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val title = state.details?.flightNumber ?: state.name ?: state.live?.icao24 ?: "Flight"
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (state.loading) {
                CircularProgressIndicator()
            }
            state.error?.let { message ->
                ElevatedCard(shape = RoundedCornerShape(18.dp)) {
                    Text(
                        message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            IdentityCard(
                title = title,
                airline = state.details?.airlineName,
                callSign = state.name?.takeIf { it != title },
                status = state.details?.status,
                icao24 = state.live?.icao24,
            )
            state.details
                ?.takeIf {
                    it.originIata != null || it.originName != null ||
                        it.destinationIata != null || it.destinationName != null
                }
                ?.let { RouteCard(it) }
            state.live?.takeIf { it.hasLiveData() }?.let { LiveCard(it) }
            state.details?.let { details ->
                val facts = detailFacts(details)
                if (facts.isNotEmpty()) FactsCard(facts)
            }
            FollowButton(
                saved = state.saved,
                enabled = viewModel.followKey != null,
                onClick = {
                    val wasSaved = state.saved
                    viewModel.toggleSaved()
                    if (!wasSaved) onFollow()
                },
            )
        }
    }
}

@Composable
private fun IdentityCard(
    title: String,
    airline: String?,
    callSign: String?,
    status: String?,
    icao24: String?,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            status?.let { StatusPill(it) }
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            airline?.let {
                Text(it, style = MaterialTheme.typography.titleMedium)
            }
            val caption = listOfNotNull(
                callSign?.let { "Callsign $it" },
                icao24?.uppercase(),
            ).joinToString(" · ")
            if (caption.isNotBlank()) {
                Text(
                    caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RouteCard(details: FlightDetails) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Route", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AirportPoint(
                    code = details.originIata ?: "—",
                    name = details.originName,
                    time = formatClock(details.departureLocal),
                    timeLabel = "Departs",
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                AirportPoint(
                    code = details.destinationIata ?: "—",
                    name = details.destinationName,
                    time = formatClock(details.arrivalLocal),
                    timeLabel = "Arrives",
                    modifier = Modifier.weight(1f),
                    alignEnd = true,
                )
            }
        }
    }
}

@Composable
private fun AirportPoint(
    code: String,
    name: String?,
    time: String,
    timeLabel: String,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
) {
    val align = if (alignEnd) Alignment.End else Alignment.Start
    val textAlign = if (alignEnd) TextAlign.End else TextAlign.Start
    Column(modifier, horizontalAlignment = align, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(code, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, textAlign = textAlign)
        name?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = textAlign,
            )
        }
        Text(time, style = MaterialTheme.typography.titleMedium, textAlign = textAlign)
        Text(
            timeLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = textAlign,
        )
    }
}

@Composable
private fun LiveCard(live: LiveTelemetry) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Live position", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Metric("Altitude", formatAltitude(live.altitudeMeters), Modifier.weight(1f))
                Metric("Speed", formatSpeed(live.speedMetersPerSecond), Modifier.weight(1f))
                Metric("Heading", formatHeading(live.headingDegrees), Modifier.weight(1f))
            }
            val notes = listOfNotNull(
                live.originCountry?.let { "Registered in $it" },
                if (live.onGround == true) "On the ground" else null,
            )
            if (notes.isNotEmpty()) {
                Text(
                    notes.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FactsCard(facts: List<Pair<String, String>>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("At the airport", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            facts.forEach { (label, value) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StatusPill(status: String) {
    val color = statusColor(status)
    Text(
        statusLabel(status),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = color,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun FollowButton(saved: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    if (saved) {
        OutlinedButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth(), shape = shape) {
            Text("Unfollow")
        }
    } else {
        Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth(), shape = shape) {
            Text("Follow flight")
        }
    }
}

private fun detailFacts(details: FlightDetails): List<Pair<String, String>> = buildList {
    details.gate?.let { add("Gate" to it) }
    details.terminal?.let { add("Terminal" to it) }
    details.baggageBelt?.let { add("Baggage belt" to it) }
    details.aircraftModel?.let { add("Aircraft" to it) }
    details.registration?.let { add("Registration" to it) }
}

private fun statusColor(status: String): Color = when (status) {
    "Delayed", "Canceled", "CanceledUncertain", "Diverted" -> Color(0xFFC44536)
    "EnRoute", "Departed", "Approaching" -> Color(0xFF1D7874)
    "Arrived" -> Color(0xFF2A6F4E)
    "Boarding", "CheckIn", "GateClosed" -> Color(0xFFC47B2B)
    else -> Color(0xFF3D5A80)
}

private fun LiveTelemetry.hasLiveData(): Boolean =
    altitudeMeters != null || speedMetersPerSecond != null || headingDegrees != null || icao24 != null
