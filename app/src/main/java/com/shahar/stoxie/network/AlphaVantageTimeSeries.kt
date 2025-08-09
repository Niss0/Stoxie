package com.shahar.stoxie.network

import com.google.gson.annotations.SerializedName

/**
 * Time series response from Alpha Vantage API.
 * Contains historical stock data in various time intervals.
 */
data class AlphaVantageTimeSeries(
    @SerializedName("Meta Data")
    val metaData: MetaData?,

    @SerializedName("Time Series (Daily)")
    val timeSeriesDaily: Map<String, TimeSeriesData>?,

    @SerializedName("Time Series (5min)")
    val timeSeries5min: Map<String, TimeSeriesData>?,

    @SerializedName("Time Series (60min)")
    val timeSeries60min: Map<String, TimeSeriesData>?,

    @SerializedName("Weekly Time Series")
    val timeSeriesWeekly: Map<String, TimeSeriesData>?,

    @SerializedName("Monthly Time Series")
    val timeSeriesMonthly: Map<String, TimeSeriesData>?
)

/**
 * Metadata for Alpha Vantage time series response.
 */
data class MetaData(
    @SerializedName("2. Symbol")
    val symbol: String
)

/**
 * Individual time series data point with OHLCV data.
 */
data class TimeSeriesData(
    @SerializedName("1. open")
    val open: String,
    @SerializedName("2. high")
    val high: String,
    @SerializedName("3. low")
    val low: String,
    @SerializedName("4. close")
    val close: String,
    @SerializedName("5. volume")
    val volume: String
)