package com.shahar.stoxie.models

/**
 * Represents a stock holding in user's portfolio.
 * Stored in Firestore under /users/{userId}/portfolio/{symbol}
 * 
 * @property symbol Stock ticker symbol (e.g., "AAPL")
 * @property shares Number of shares owned (supports fractional shares)
 * @property averageBuyPrice Average cost per share
 * @property companyName Company display name
 */
data class PortfolioHolding(
    val symbol: String = "",
    val shares: Double = 0.0,
    val averageBuyPrice: Double = 0.0,
    val companyName: String = ""
)