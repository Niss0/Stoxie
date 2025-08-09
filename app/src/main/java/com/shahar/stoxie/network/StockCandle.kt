package com.shahar.stoxie.network

import com.google.gson.annotations.SerializedName

/**
 * Historical candlestick data from Finnhub API.
 * Contains OHLC (Open, High, Low, Close) time series data.
 */
data class StockCandle(
    /** Closing prices */
    @SerializedName("c")
    val close: List<Double>,
    /** High prices */
    @SerializedName("h")
    val high: List<Double>,
    /** Low prices */
    @SerializedName("l")
    val low: List<Double>,
    /** Opening prices */
    @SerializedName("o")
    val open: List<Double>,
    /** Unix timestamps */
    @SerializedName("t")
    val timestamp: List<Long>,
    /** API response status */
    @SerializedName("s")
    val status: String
)