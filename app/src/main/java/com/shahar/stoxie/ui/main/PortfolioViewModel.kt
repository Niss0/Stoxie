package com.shahar.stoxie.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieEntry
import com.shahar.stoxie.data.PortfolioRepository
import com.shahar.stoxie.data.StockRepository
import com.shahar.stoxie.models.Stock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

// State definition remains the same
sealed class PortfolioState {
    data class Success(
        val stocks: List<Stock>,
        val allocationChartEntries: List<PieEntry>,
        val sectorChartEntries: List<PieEntry>,
        val totalValue: Double,
        val totalCost: Double,
        val totalGainLoss: Double,
        val totalGainLossPercent: Double
    ) : PortfolioState()
    data class Error(val message: String) : PortfolioState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class PortfolioViewModel : ViewModel() {

    private val stockRepository = StockRepository()
    private val portfolioRepository = PortfolioRepository()

    // NEW: This flow holds the item that is waiting for permanent deletion.
    private val _stockPendingDeletion = MutableStateFlow<Stock?>(null)

    val portfolioState = portfolioRepository.getPortfolioHoldingsFlow()
        // Combine the database holdings with our temporary "pending deletion" state
        ?.combine(_stockPendingDeletion) { holdings, pendingDeletion ->
            // If there's an item pending deletion, filter it out from the list
            // before passing it to the rest of the flow.
            if (pendingDeletion != null) {
                holdings.filter { it.symbol != pendingDeletion.symbol }
            } else {
                holdings
            }
        }
        ?.flatMapLatest { holdings ->
            flow {
                if (holdings.isEmpty()) {
                    emit(PortfolioState.Success(emptyList(), emptyList(), emptyList(), 0.0, 0.0, 0.0, 0.0))
                    return@flow
                }

                try {
                    val stocksWithDetails = holdings.map { holding ->
                        viewModelScope.async(Dispatchers.IO) {
                            val quote = stockRepository.getStockQuote(holding.symbol)
                            val profile = stockRepository.getCompanyProfile(holding.symbol)
                            Stock(
                                symbol = holding.symbol,
                                companyName = profile?.name ?: holding.companyName,
                                shares = holding.shares,
                                averageBuyPrice = holding.averageBuyPrice,
                                quote = quote,
                                sector = profile?.sector ?: "Other"
                            )
                        }
                    }.awaitAll().filter { it.quote != null }

                    val totalCurrentValue = stocksWithDetails.sumOf { it.shares * (it.quote?.currentPrice ?: 0.0) }
                    val totalCostBasis = stocksWithDetails.sumOf { it.shares * it.averageBuyPrice }
                    val overallGainLoss = totalCurrentValue - totalCostBasis
                    val overallGainLossPercent = if (totalCostBasis > 0) (overallGainLoss / totalCostBasis) * 100 else 0.0

                    val allocationEntries = if (totalCurrentValue > 0) {
                        stocksWithDetails.map { stock ->
                            val holdingValue = stock.shares * (stock.quote?.currentPrice ?: 0.0)
                            PieEntry((holdingValue / totalCurrentValue * 100).toFloat(), stock.symbol)
                        }
                    } else {
                        emptyList()
                    }

                    val sectorValueMap = stocksWithDetails.groupBy { it.sector }
                        .mapValues { (_, stocks) -> stocks.sumOf { it.shares * (it.quote?.currentPrice ?: 0.0) } }

                    val sectorEntries = if (totalCurrentValue > 0) {
                        sectorValueMap.map { (sector, value) ->
                            PieEntry((value / totalCurrentValue * 100).toFloat(), sector)
                        }
                    } else {
                        emptyList()
                    }


                    emit(PortfolioState.Success(
                        stocks = stocksWithDetails,
                        allocationChartEntries = allocationEntries,
                        sectorChartEntries = sectorEntries,
                        totalValue = totalCurrentValue,
                        totalCost = totalCostBasis,
                        totalGainLoss = overallGainLoss,
                        totalGainLossPercent = overallGainLossPercent
                    ))
                } catch (e: Exception) {
                    Log.e("PortfolioViewModel", "Error fetching portfolio details", e)
                    emit(PortfolioState.Error("Failed to fetch stock data"))
                }
            }
        }
        ?.asLiveData()

    fun addStock(symbol: String, shares: Double, price: Double) = viewModelScope.launch {
        try {
            val profile = stockRepository.getCompanyProfile(symbol)
            val companyName = profile?.name ?: symbol
            portfolioRepository.addStockToPortfolio(symbol, companyName, shares, price)
        } catch (e: Exception) {
            Log.e("PortfolioViewModel", "Error adding stock", e)
        }
    }

    // --- NEW STATE MANAGEMENT FUNCTIONS ---

    // Called from onSwiped. It just sets the state. Does NOT delete from DB.
    fun onStockSwiped(stock: Stock) {
        _stockPendingDeletion.value = stock
    }

    // Called from Snackbar "Undo" action.
    fun cancelDeletion() {
        _stockPendingDeletion.value = null
    }

    // Called when Snackbar dismisses. This is the only place we permanently delete.
    fun confirmDeletion() {
        viewModelScope.launch {
            _stockPendingDeletion.value?.let { stockToDelete ->
                portfolioRepository.removeStockFromPortfolio(stockToDelete.symbol)
            }
            _stockPendingDeletion.value = null
        }
    }
}