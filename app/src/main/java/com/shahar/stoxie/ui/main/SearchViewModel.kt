package com.shahar.stoxie.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahar.stoxie.data.StockRepository
import com.shahar.stoxie.models.SymbolLookupResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * States for stock search operations.
 * Manages UI feedback during search and filtering processes.
 */
sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val results: List<SymbolLookupResult>) : SearchState()
    data class Error(val message: String) : SearchState()
}

/**
 * ViewModel for stock search functionality.
 * Handles debounced search queries and result filtering.
 */
class SearchViewModel : ViewModel() {
    private val stockRepository = StockRepository()
    private var searchJob: Job? = null

    /**
     * State management for search operations.
     * Provides UI feedback during search and filtering.
     */
    private val _searchState = MutableLiveData<SearchState>(SearchState.Idle)
    val searchState: LiveData<SearchState> = _searchState

    /**
     * Handles search query changes with debouncing.
     * Cancels previous searches and filters results for common stocks only.
     * @param query The search query from user input
     */
    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.length < 2) {
            _searchState.value = SearchState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // Debounce: wait 300ms after user stops typing
            _searchState.postValue(SearchState.Loading)
            try {
                val result = stockRepository.searchStocks(query)
                if (result != null) {
                    // Filter out crypto and foreign exchange symbols
                    val filteredResults = result.results.filter {
                        !it.type.contains("Crypto") && !it.symbol.contains(".")
                    }
                    _searchState.postValue(SearchState.Success(filteredResults))
                } else {
                    _searchState.postValue(SearchState.Error("Search failed"))
                }
            } catch (e: Exception) {
                _searchState.postValue(SearchState.Error(e.message ?: "An error occurred"))
            }
        }
    }
}