package com.jetwatch.ui.search

import androidx.lifecycle.viewModelScope
import com.jetwatch.ui.JetViewModel
import com.jetwatch.domain.model.SearchResults
import com.jetwatch.domain.repository.FlightRepository
import com.jetwatch.domain.search.SearchQueryParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val loading: Boolean = false,
    val results: SearchResults = SearchResults(),
    val error: String? = null,
    val searched: Boolean = false,
)

class SearchViewModel(
    private val flights: FlightRepository,
) : JetViewModel() {
    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }
    }

    fun search(raw: String = _state.value.query) {
        val query = raw.trim()
        if (query.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(query = query, loading = true, error = null, searched = true) }
            val result = runCatching { flights.search(SearchQueryParser.parse(query)) }
            _state.update {
                it.copy(
                    loading = false,
                    results = result.getOrNull() ?: SearchResults(),
                    error = result.exceptionOrNull()?.message,
                )
            }
        }
    }
}
