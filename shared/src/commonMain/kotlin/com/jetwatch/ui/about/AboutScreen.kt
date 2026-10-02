package com.jetwatch.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.jetwatch.ui.BackTopBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

private const val PRIVACY_POLICY_URL =
    "https://github.com/Diana-Kieru/JetWatch/blob/main/docs/privacy-policy.md"
private const val TERMS_OF_USE_URL =
    "https://github.com/Diana-Kieru/JetWatch/blob/main/docs/terms-of-use.md"

@Composable
fun AboutScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { BackTopBar("About", onBack) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "JetWatch is a flight tracker. The map shows live aircraft, and a flight page fills in the number, airline, origin, destination, gate, and status. Follow a flight and this phone notifies you when it is delayed, departs, or lands.",
        )
        Section(title = "Data sources") {
            Credit(
                name = "OpenSky Network",
                detail = "Live aircraft positions for the area on the map.",
                url = "https://opensky-network.org/",
            )
            Credit(
                name = "AeroDataBox",
                detail = "Flight schedules, search, and the checks behind alerts. Requests go through RapidAPI.",
                url = "https://www.aerodatabox.com/",
            )
            Credit(
                name = "OpenFreeMap",
                detail = "Map tiles, using the Liberty style.",
                url = "https://openfreemap.org/",
            )
            Credit(
                name = "MapLibre",
                detail = "Draws the map on Android.",
                url = "https://maplibre.org/",
            )
        }
        Section(title = "Privacy and terms") {
            Link(label = "Privacy policy", url = PRIVACY_POLICY_URL)
            Link(label = "Terms of use", url = TERMS_OF_USE_URL)
        }
        Section(title = "Delete your data") {
            Text(
                "JetWatch does not create an account, so there is no username or password to close. Followed flights, the last map positions, and cached flight details stay on this phone.",
            )
            Text("To remove one flight, open Saved and tap Unfollow.")
            Text(
                "To delete everything JetWatch stored, uninstall the app, or open Settings, then Apps, JetWatch, Storage, and Clear data.",
            )
        }
    }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun Credit(name: String, detail: String, url: String) {
    val uriHandler = LocalUriHandler.current
    Column {
        TextButton(onClick = { uriHandler.openUri(url) }) {
            Text(name)
        }
        Text(detail, modifier = Modifier.padding(horizontal = 12.dp))
    }
}

@Composable
private fun Link(label: String, url: String) {
    val uriHandler = LocalUriHandler.current
    TextButton(onClick = { uriHandler.openUri(url) }) {
        Text(label)
    }
}
