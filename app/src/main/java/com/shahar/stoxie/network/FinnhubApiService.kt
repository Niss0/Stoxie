package com.shahar.stoxie.network

import com.shahar.stoxie.models.SymbolLookup
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for Finnhub API endpoints.
 * Provides access to stock quotes, company profiles, search, and historical data.
 */
interface FinnhubApiService {

    /**
     * Fetches real-time stock quote.
     * @param symbol Stock symbol (e.g., "AAPL")
     * @param apiKey Finnhub API key
     * @return StockQuote with current price data
     */
    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("token") apiKey: String
    ): StockQuote

    /**
     * Fetches company profile information.
     * @param symbol Stock symbol
     * @param apiKey Finnhub API key
     * @return CompanyProfile with company details
     */
    @GET("stock/profile2")
    suspend fun getCompanyProfile(
        @Query("symbol") symbol: String,
        @Query("token") apiKey: String
    ): CompanyProfile

    /**
     * Searches for stock symbols by query.
     * @param query Search term (company name or symbol)
     * @param apiKey Finnhub API key
     * @return SymbolLookup with search results
     */
    @GET("search")
    suspend fun searchSymbol(
        @Query("q") query: String,
        @Query("token") apiKey: String
    ): SymbolLookup

    /**
     * Fetches historical candlestick data.
     * @param symbol Stock symbol
     * @param resolution Data resolution (1, 5, 15, 30, 60, D, W, M)
     * @param from Start timestamp (Unix)
     * @param to End timestamp (Unix)
     * @param apiKey Finnhub API key
     * @return StockCandle with OHLC data
     */
    @GET("stock/candle")
    suspend fun getStockCandles(
        @Query("symbol") symbol: String,
        @Query("resolution") resolution: String,
        @Query("from") from: Long,
        @Query("to") to: Long,
        @Query("token") apiKey: String
    ): StockCandle
}