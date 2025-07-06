package com.shahar.stoxie.models

/**
 * Represents a single holding (a specific stock) within a user's portfolio.
 *
 * These objects would be stored in a sub-collection under each User.
 * For example: /users/{userId}/portfolio/{holdingId}
 *
 * @property symbol The stock ticker symbol, e.g., "AAPL". This is the primary identifier.
 * @property shares The number of shares the user owns. A Double allows for fractional shares.
 * @property averageBuyPrice The average price the user paid per share.
 * @property companyName The full name of the company, e.g., "Apple Inc.". We can fetch this once when the user adds the stock.
 */

data class PortfolioHolding(
    val symbol: String = "",
    val shares: Double = 0.0,
    val averageBuyPrice: Double = 0.0,
    val companyName: String = ""
)