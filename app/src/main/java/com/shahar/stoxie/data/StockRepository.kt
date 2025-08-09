package com.shahar.stoxie.data

import android.util.Log
import com.shahar.stoxie.BuildConfig
import com.shahar.stoxie.network.*
import com.shahar.stoxie.models.SymbolLookup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Repository for stock data operations.
 * Provides clean API for accessing stock data from Finnhub and Alpha Vantage APIs.
 */
class StockRepository {

    private val finnhubApiService = FinnhubClient.api
    private val alphaVantageApiService = AlphaVantageClient.api
    private val TAG = "StockRepository"

    /**
     * Fetches real-time stock quote for a symbol.
     * @param symbol Stock symbol (e.g., "AAPL")
     * @return StockQuote with current price and change data, or null if failed
     */
    suspend fun getStockQuote(symbol: String): StockQuote? {
        return withContext(Dispatchers.IO) {
            try {
                finnhubApiService.getQuote(symbol, BuildConfig.FINNHUB_API_KEY)
            } catch (e: Exception) {
                if (e is HttpException) {
                    when (e.code()) {
                        429 -> Log.e(TAG, "Rate limit reached for getStockQuote ($symbol). HTTP 429 Too Many Requests.")
                        403 -> Log.e(TAG, "Forbidden (HTTP 403) for getStockQuote ($symbol): Check your API key, quota, or endpoint permissions.")
                        else -> Log.e(TAG, "HTTP error ${e.code()} for getStockQuote ($symbol): ${e.message()}")
                    }
                } else {
                    Log.e(TAG, "getStockQuote for symbol $symbol failed", e)
                }
                null
            }
        }
    }

    /**
     * Fetches company profile information.
     * @param symbol Stock symbol to get company info for
     * @return CompanyProfile with company details, or null if failed
     */
    suspend fun getCompanyProfile(symbol: String): CompanyProfile? {
        return withContext(Dispatchers.IO) {
            try {
                finnhubApiService.getCompanyProfile(symbol, BuildConfig.FINNHUB_API_KEY)
            } catch (e: Exception) {
                if (e is HttpException) {
                    when (e.code()) {
                        429 -> Log.e(TAG, "Rate limit reached for getCompanyProfile ($symbol). HTTP 429 Too Many Requests.")
                        403 -> Log.e(TAG, "Forbidden (HTTP 403) for getCompanyProfile ($symbol): Check your API key, quota, or endpoint permissions.")
                        else -> Log.e(TAG, "HTTP error ${e.code()} for getCompanyProfile ($symbol): ${e.message()}")
                    }
                } else {
                    Log.e(TAG, "getCompanyProfile for symbol $symbol failed", e)
                }
                null
            }
        }
    }

    /**
     * Searches for stocks by company name or symbol.
     * @param query Search query (company name or symbol)
     * @return SymbolLookup with matching results, or null if failed
     */
    suspend fun searchStocks(query: String): SymbolLookup? {
        return withContext(Dispatchers.IO) {
            try {
                finnhubApiService.searchSymbol(query, BuildConfig.FINNHUB_API_KEY)
            } catch (e: Exception) {
                if (e is HttpException) {
                    when (e.code()) {
                        429 -> Log.e(TAG, "Rate limit reached for searchStocks ('$query'). HTTP 429 Too Many Requests.")
                        403 -> Log.e(TAG, "Forbidden (HTTP 403) for searchStocks ('$query'): Check your API key, quota, or endpoint permissions.")
                        else -> Log.e(TAG, "HTTP error ${e.code()} for searchStocks ('$query'): ${e.message()}")
                    }
                } else {
                    Log.e(TAG, "searchStocks for query '$query' failed", e)
                }
                null
            }
        }
    }

    /**
     * Fetches historical time series data for charting.
     * @param symbol Stock symbol
     * @param function Alpha Vantage function (e.g., "TIME_SERIES_DAILY")
     * @param interval Time interval for intraday data (e.g., "5min"), null for daily/weekly
     * @return AlphaVantageTimeSeries with historical data, or null if failed
     */
    suspend fun getAlphaVantageTimeSeries(symbol: String, function: String, interval: String?): AlphaVantageTimeSeries? {
        return withContext(Dispatchers.IO) {
            try {
                alphaVantageApiService.getTimeSeries(
                    function = function,
                    symbol = symbol,
                    interval = interval,
                    apiKey = BuildConfig.ALPHA_VANTAGE_API_KEY
                )
            } catch (e: Exception) {
                Log.e(TAG, "getAlphaVantageTimeSeries failed", e)
                null
            }
        }
    }

    /**
     * Gets historical stock data from Alpha Vantage.
     * @param symbol Stock symbol
     * @param period One of: "1D", "5D", "1M", "3M", "1Y", "5Y"
     * @return List of Triple(timestamp, open, close) or null if failed
     */
    suspend fun getAlphaVantageStockHistory(symbol: String, period: String): List<Triple<Long, Double, Double>>? {
        return withContext(Dispatchers.IO) {
            try {
                val (function, interval) = when (period) {
                    "1D" -> "TIME_SERIES_INTRADAY" to "5min"
                    "5D" -> "TIME_SERIES_DAILY" to null
                    "1M" -> "TIME_SERIES_DAILY" to null
                    "3M" -> "TIME_SERIES_DAILY" to null
                    "1Y" -> "TIME_SERIES_WEEKLY" to null
                    "5Y" -> "TIME_SERIES_MONTHLY" to null
                    else -> "TIME_SERIES_DAILY" to null
                }
                
                val timeSeries = alphaVantageApiService.getTimeSeries(
                    function = function,
                    symbol = symbol,
                    interval = interval,
                    apiKey = BuildConfig.ALPHA_VANTAGE_API_KEY
                )
                
                val data = when (function) {
                    "TIME_SERIES_INTRADAY" -> timeSeries.timeSeries5min
                    "TIME_SERIES_DAILY" -> timeSeries.timeSeriesDaily
                    "TIME_SERIES_WEEKLY" -> timeSeries.timeSeriesWeekly
                    "TIME_SERIES_MONTHLY" -> timeSeries.timeSeriesMonthly
                    else -> timeSeries.timeSeriesDaily
                }
                
                if (data == null || data.isEmpty()) {
                    Log.e(TAG, "No data received from Alpha Vantage")
                    return@withContext null
                }
                
                val entries = data.mapNotNull { (dateStr, timeSeriesData) ->
                    try {
                        val timestamp = when (function) {
                            "TIME_SERIES_INTRADAY" -> {
                                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                                dateFormat.parse(dateStr)?.time ?: 0
                            }
                            else -> {
                                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                                dateFormat.parse(dateStr)?.time ?: 0
                            }
                        }
                        
                        val open = timeSeriesData.open.toDoubleOrNull()
                        val close = timeSeriesData.close.toDoubleOrNull()
                        
                        if (open != null && close != null && open > 0 && close > 0 && timestamp > 0) {
                            Triple(timestamp, open, close)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing data point: $dateStr", e)
                        null
                    }
                }.sortedBy { it.first }
                
                val filteredResult = when (period) {
                    "1D" -> entries.takeLast(100)
                    "5D" -> entries.takeLast(5)
                    "1M" -> entries.takeLast(30)
                    "3M" -> entries.takeLast(90)
                    "1Y" -> entries.takeLast(52)
                    "5Y" -> entries.takeLast(60)
                    else -> entries.takeLast(30)
                }.reversed()
                
                filteredResult
                
            } catch (e: Exception) {
                Log.e(TAG, "getAlphaVantageStockHistory failed for symbol=$symbol, period=$period", e)
                null
            }
        }
    }
}