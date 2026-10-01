package com.jetwatch.ui.map

import androidx.lifecycle.viewModelScope
import com.jetwatch.ui.JetViewModel
import com.jetwatch.domain.model.Aircraft
import com.jetwatch.domain.model.BoundingBox
import com.jetwatch.domain.repository.AircraftRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapUiState(
    val aircraft: List<Aircraft> = emptyList(),
    val selected: Aircraft? = null,
    val loading: Boolean = false,
    val fromCache: Boolean = false,
    val message: String? = null,
)

@OptIn(FlowPreview::class)
class MapViewModel(
    private val aircraftRepository: AircraftRepository,
) : JetViewModel() {
    private val active = MutableStateFlow(false)
    private val bounds = MutableStateFlow<BoundingBox?>(null)
    private val selection = MutableStateFlow<Aircraft?>(null)
    private val snapshot = MutableStateFlow(MapUiState())

    val state: StateFlow<MapUiState> = combine(snapshot, selection) { base, selected ->
        base.copy(selected = selected)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MapUiState())

    init {
        viewModelScope.launch {
            combine(active, bounds.debounce(600)) { isActive, box ->
                if (isActive) box else null
            }.collectLatest { box ->
                if (box == null) return@collectLatest
                while (true) {
                    if (box.isTooWide()) {
                        snapshot.update {
                            it.copy(
                                loading = false,
                                aircraft = emptyList(),
                                fromCache = false,
                                message = "Zoom in to load aircraft for this area.",
                            )
                        }
                    } else {
                        refresh(box)
                    }
                    delay(POLL_INTERVAL_MS)
                }
            }
        }
    }

    fun setActive(isActive: Boolean) {
        active.value = isActive
    }

    fun updateBounds(box: BoundingBox) {
        bounds.value = box
    }

    fun select(aircraft: Aircraft) {
        selection.value = aircraft
    }

    fun clearSelection() {
        selection.value = null
    }

    private suspend fun refresh(box: BoundingBox) {
        snapshot.update { it.copy(loading = it.aircraft.isEmpty()) }
        val result = aircraftRepository.load(box)
        snapshot.update {
            it.copy(
                aircraft = result.aircraft,
                loading = false,
                fromCache = result.fromCache,
                message = result.message,
            )
        }
        val selectedId = selection.value?.icao24 ?: return
        selection.value = result.aircraft.find { it.icao24 == selectedId } ?: selection.value
    }

    private companion object {
        const val POLL_INTERVAL_MS = 12_000L
    }
}
