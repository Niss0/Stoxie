package com.shahar.stoxie.models

import com.shahar.stoxie.network.StockQuote

/**
 * Represents a stock in user's portfolio.
 * Combines portfolio holdings with real-time market data.
 */
data class Stock(
    /** Stock symbol (e.g., "AAPL") */
    val symbol: String,
    /** Company name for display */
    val companyName: String,
    /** Number of shares owned */
    val shares: Double,
    /** Average purchase price per share */
    val averageBuyPrice: Double,
    /** Real-time market data (nullable for loading states) */
    val quote: StockQuote? = null,
    /** Industry sector for portfolio analysis */
    val sector: String = "N/A"
)