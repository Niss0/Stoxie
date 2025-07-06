package com.shahar.stoxie.network

import com.google.gson.annotations.SerializedName

/**
 * Represents the data structure for a stock quote from the Finnhub API.
 * The @SerializedName annotation is used by Gson to map the JSON key
 * from the API response to the corresponding field in our Kotlin class.
 */
data class StockQuote(

    // Maps the "c" key (Current price) from JSON to our "currentPrice" field
    @SerializedName("c")
    val currentPrice: Double,

    // Maps the "d" key (Change) from JSON
    @SerializedName("d")
    val change: Double,

    // Maps the "dp" key (Percent change) from JSON
    @SerializedName("dp")
    val percentChange: Double,

    // Maps the "pc" key (Previous close price) from JSON
    @SerializedName("pc")
val previousClose: Double
)