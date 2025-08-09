package com.shahar.stoxie.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for Alpha Vantage API.
 * Provides historical time series data for stock charts.
 */
interface AlphaVantageApiService {
    /**
     * Fetches time series data for stocks.
     * @param function Alpha Vantage function (TIME_SERIES_DAILY, etc.)
     * @param symbol Stock symbol
     * @param apiKey Alpha Vantage API key
     * @param interval Time interval for intraday data (optional)
     * @return Time series data
     */
    @GET("query")
    suspend fun getTimeSeries(
        @Query("function") function: String,
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String,
        @Query("interval") interval: String? = null
    ): AlphaVantageTimeSeries
}

/**
 * Retrofit client for Alpha Vantage API.
 * Singleton with lazy initialization for historical stock data.
 */
object AlphaVantageClient {
    private const val BASE_URL = "https://www.alphavantage.co/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: AlphaVantageApiService by lazy {
        retrofit.create(AlphaVantageApiService::class.java)
    }
}