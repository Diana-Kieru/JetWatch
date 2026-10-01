package com.jetwatch.ui.saved

import androidx.lifecycle.viewModelScope
import com.jetwatch.ui.JetViewModel
import com.jetwatch.domain.model.SavedFlight
import com.jetwatch.domain.repository.SavedFlightRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedFlightsViewModel(
    private val savedFlights: SavedFlightRepository,
) : JetViewModel() {
    val flights: StateFlow<List<SavedFlight>> = savedFlights.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun remove(key: String) {
        viewModelScope.launch { savedFlights.remove(key) }
    }
}
