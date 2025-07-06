package com.shahar.stoxie.models

import com.shahar.stoxie.network.StockQuote

/**
 * Represents a single stock in a user's portfolio, combining the symbol
 * with the latest quote data fetched from the API.
 */
data class Stock(
    val symbol: String,
    val companyName: String,
    val shares: Double,
    val averageBuyPrice: Double,
    val quote: StockQuote? = null,
    val sector: String = "N/A"
)