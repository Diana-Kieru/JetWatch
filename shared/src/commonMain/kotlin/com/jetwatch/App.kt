package com.jetwatch

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.vector.ImageVector
import com.jetwatch.ui.JetViewModel
import com.jetwatch.ui.LogoSplash
import com.jetwatch.ui.about.AboutScreen
import com.jetwatch.ui.details.FlightDetailsScreen
import com.jetwatch.ui.details.FlightDetailsViewModel
import com.jetwatch.ui.map.MapScreen
import com.jetwatch.ui.map.MapViewModel
import com.jetwatch.ui.navigation.DetailsRequest
import com.jetwatch.ui.saved.SavedFlightsScreen
import com.jetwatch.ui.saved.SavedFlightsViewModel
import com.jetwatch.ui.search.SearchScreen
import com.jetwatch.ui.search.SearchViewModel
import com.jetwatch.ui.theme.JetWatchTheme
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.launch

private sealed interface Screen {
    data object Map : Screen
    data object Search : Screen
    data object Saved : Screen
    data object About : Screen
    data class Details(val request: DetailsRequest) : Screen
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App(
    graph: JetWatchGraph,
    openFlightKey: String? = null,
    onFollowFlight: () -> Unit = {},
    onExit: () -> Unit = {},
) {
    JetWatchTheme(dynamicColor = false) {
        var showSplash by remember { mutableStateOf(openFlightKey.isNullOrBlank()) }
        if (showSplash) {
            LogoSplash(onFinished = { showSplash = false })
            return@JetWatchTheme
        }

        var stack by remember {
            mutableStateOf(
                if (openFlightKey.isNullOrBlank()) {
                    listOf<Screen>(Screen.Map)
                } else {
                    listOf(Screen.Map, Screen.Details(DetailsRequest(lookup = openFlightKey)))
                },
            )
        }
        val current = stack.last()
        val showBars = current !is Screen.Details
        var confirmExit by remember { mutableStateOf(false) }
        var lastBack by remember { mutableStateOf<TimeMark?>(null) }
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val goBack: () -> Unit = {
            if (confirmExit) {
                confirmExit = false
            } else {
                val atRoot = stack.size <= 1 && stack.last() is Screen.Map
                val pressedAgain = lastBack?.let { it.elapsedNow() < 2.seconds } == true
                if (!atRoot) {
                    stack = if (stack.size > 1) stack.dropLast(1) else listOf(Screen.Map)
                    lastBack = TimeSource.Monotonic.markNow()
                } else if (pressedAgain) {
                    lastBack = null
                    snackbarHostState.currentSnackbarData?.dismiss()
                    confirmExit = true
                } else {
                    lastBack = TimeSource.Monotonic.markNow()
                    scope.launch {
                        snackbarHostState.showSnackbar("Press back again to exit")
                    }
                    Unit
                }
            }
        }
        BackHandler(enabled = true, onBack = goBack)
        val mapViewModel = rememberJetViewModel("map") { MapViewModel(graph.aircraft) }
        val searchViewModel = rememberJetViewModel("search") { SearchViewModel(graph.flights) }
        val savedViewModel = rememberJetViewModel("saved") { SavedFlightsViewModel(graph.saved) }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (showBars) {
                    NavigationBar {
                        NavItem("Map", current is Screen.Map, Icons.Filled.Place) {
                            stack = listOf(Screen.Map)
                        }
                        NavItem("Search", current is Screen.Search, Icons.Filled.Search) {
                            stack = listOf(Screen.Search)
                        }
                        NavItem("Saved", current is Screen.Saved, Icons.Filled.Star) {
                            stack = listOf(Screen.Saved)
                        }
                        NavItem("About", current is Screen.About, Icons.Filled.Info) {
                            stack = listOf(Screen.About)
                        }
                    }
                }
            },
        ) { padding ->
            when (val screen = current) {
                Screen.Map -> MapScreen(
                    onOpenAircraft = { aircraft -> stack = stack + Screen.Details(DetailsRequest.from(aircraft)) },
                    viewModel = mapViewModel,
                    modifier = Modifier.padding(padding),
                )
                Screen.Search -> SearchScreen(
                    onBack = goBack,
                    onOpenFlight = { number -> stack = stack + Screen.Details(DetailsRequest(lookup = number)) },
                    viewModel = searchViewModel,
                    modifier = Modifier.padding(padding),
                )
                Screen.Saved -> SavedFlightsScreen(
                    onBack = goBack,
                    onOpenFlight = { key -> stack = stack + Screen.Details(DetailsRequest(lookup = key)) },
                    viewModel = savedViewModel,
                    modifier = Modifier.padding(padding),
                )
                Screen.About -> AboutScreen(
                    onBack = goBack,
                    modifier = Modifier.padding(padding),
                )
                is Screen.Details -> FlightDetailsScreen(
                    onBack = goBack,
                    onFollow = onFollowFlight,
                    viewModel = rememberJetViewModel(screen.request) {
                        FlightDetailsViewModel(screen.request, graph.flights, graph.saved)
                    },
                    modifier = Modifier.padding(padding),
                )
            }
        }
        if (confirmExit) {
            AlertDialog(
                onDismissRequest = { confirmExit = false },
                title = { Text("Exit JetWatch?") },
                text = { Text("Do you want to exit the app?") },
                confirmButton = {
                    TextButton(onClick = onExit) { Text("Exit") }
                },
                dismissButton = {
                    TextButton(onClick = { confirmExit = false }) { Text("Stay") }
                },
            )
        }
    }
}

@Composable
private fun <T : JetViewModel> rememberJetViewModel(key: Any, factory: () -> T): T {
    val viewModel = remember(key) { factory() }
    DisposableEffect(viewModel) {
        onDispose { viewModel.close() }
    }
    return viewModel
}

@Composable
private fun RowScope.NavItem(
    label: String,
    selected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) },
    )
}
