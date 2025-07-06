package com.shahar.stoxie.data

import com.shahar.stoxie.BuildConfig
import com.shahar.stoxie.network.CompanyProfile
import com.shahar.stoxie.network.FinnhubClient
import com.shahar.stoxie.network.StockQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StockRepository {

    private val finnhubApiService = FinnhubClient.api

    /**
     * Fetches a real-time quote for a given stock symbol from the Finnhub API.
     * This function is safe to call from a ViewModel's coroutine scope.
     *
     * @param symbol The stock symbol to fetch (e.g., "AAPL").
     * @return A StockQuote object on success, or null on failure.
     */
    suspend fun getStockQuote(symbol: String): StockQuote? {
        // Use withContext to switch to an I/O-optimized thread for the network call.
        return withContext(Dispatchers.IO) {
            try {
                // Call the suspend function from our Retrofit service interface.
                // We access the API key securely from the generated BuildConfig class.
                finnhubApiService.getQuote(symbol, BuildConfig.FINNHUB_API_KEY)
            } catch (e: Exception) {
                // In case of a network error, log it and return null.
                // A more robust implementation would differentiate between error types.
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun getCompanyProfile(symbol: String): CompanyProfile? {
        return withContext(Dispatchers.IO) {
            try {
                finnhubApiService.getCompanyProfile(symbol, BuildConfig.FINNHUB_API_KEY)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}