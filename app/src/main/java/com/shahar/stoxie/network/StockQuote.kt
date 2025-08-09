package com.shahar.stoxie.network

import com.google.gson.annotations.SerializedName

/**
 * Real-time stock quote data from Finnhub API.
 * Maps JSON response fields to Kotlin properties using @SerializedName.
 */
data class StockQuote(

    /**
     * Current market price.
     * Maps to "c" field in API response.
     */
    @SerializedName("c")
    val currentPrice: Double,

    /**
     * Absolute price change from previous close.
     * Maps to "d" field in API response.
     */
    @SerializedName("d")
    val change: Double,

    /**
     * Percentage change from previous close.
     * Maps to "dp" field in API response.
     */
    @SerializedName("dp")
    val percentChange: Double,

    /**
     * Previous day's closing price.
     * Maps to "pc" field in API response.
     */
    @SerializedName("pc")
    val previousClose: Double
)