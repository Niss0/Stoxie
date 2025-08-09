package com.shahar.stoxie.ui.main
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.shahar.stoxie.data.StockRepository
import com.shahar.stoxie.network.CompanyProfile
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Stock details data for UI display.
 */
data class StockDetailsData(
    val profile: CompanyProfile,
    val chartEntries: List<Entry>?,
    val currentPrice: Float?,
    val priceChange: Float?,
    val priceChangePercent: Float?
)

/**
 * UI states for stock details loading.
 */
sealed class StockDetailsState {
    object Loading : StockDetailsState()
    data class Success(val data: StockDetailsData) : StockDetailsState()
    data class Error(val message: String) : StockDetailsState()
}

/**
 * ViewModel for stock details screen.
 * Manages loading of company profile and historical chart data.
 */
class StockDetailsViewModel : ViewModel() {
    private val repository = StockRepository()
    private val TAG = "StockDetailsViewModel"

    private val _state = MutableLiveData<StockDetailsState>()
    val state: LiveData<StockDetailsState> = _state

    /**
     * Loads stock details including profile and historical data.
     * @param symbol Stock symbol to load
     * @param period Time period for chart data (1D, 5D, 1M, 3M, 1Y, 5Y)
     */
    fun loadStockDetails(symbol: String, period: String = "1Y") {
        viewModelScope.launch {
            _state.postValue(StockDetailsState.Loading)
            try {
                val profileDeferred = async { repository.getCompanyProfile(symbol) }
                val historyDeferred = async { repository.getAlphaVantageStockHistory(symbol, period) }

                val profile = profileDeferred.await()
                val history = historyDeferred.await()

                if (profile == null) {
                    _state.postValue(StockDetailsState.Error("Could not load company profile for $symbol."))
                    return@launch
                }

                if (history == null || history.isEmpty()) {
                    _state.postValue(StockDetailsState.Error("Could not fetch historical data for this period."))
                    return@launch
                }

                val entries = history.mapNotNull { (timestamp, _, close) ->
                    if (timestamp <= 0 || close <= 0) null
                    else Entry(timestamp.toFloat(), close.toFloat())
                }
                
                if (entries.isEmpty()) {
                    _state.postValue(StockDetailsState.Error("No valid chart data available."))
                    return@launch
                }
                
                val oldest = history.last()
                val newest = history.first()
                val startPrice = oldest.second
                val currentPrice = newest.third
                val priceChange = currentPrice - startPrice
                val priceChangePercent = if (startPrice != 0.0) (priceChange / startPrice) * 100.0 else 0.0

                _state.postValue(StockDetailsState.Success(
                    StockDetailsData(
                        profile = profile,
                        chartEntries = entries,
                        currentPrice = currentPrice.toFloat(),
                        priceChange = priceChange.toFloat(),
                        priceChangePercent = priceChangePercent.toFloat()
                    )
                ))
            } catch (e: Exception) {
                Log.e(TAG, "Error loading stock details", e)
                _state.postValue(StockDetailsState.Error("Failed to load stock details: ${e.message}"))
            }
        }
    }
}