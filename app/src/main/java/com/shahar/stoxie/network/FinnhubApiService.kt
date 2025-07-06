package com.shahar.stoxie.network

import retrofit2.http.GET
import retrofit2.http.Query

interface FinnhubApiService {

    /**
     * Fetches a real-time quote for a given stock symbol.
     * @param symbol The stock symbol (e.g., "AAPL").
     * @return A StockQuote object.
     */
    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("token") apiKey: String
    ): StockQuote

    @GET("stock/profile2")
    suspend fun getCompanyProfile(
        @Query("symbol") symbol: String,
        @Query("token") apiKey: String
    ): CompanyProfile
}