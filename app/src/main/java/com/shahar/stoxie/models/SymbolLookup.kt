package com.shahar.stoxie.models

import com.google.gson.annotations.SerializedName

/**
 * Individual search result from Finnhub symbol lookup.
 * @property description Company description
 * @property displaySymbol Display symbol for UI
 * @property symbol Stock symbol
 * @property type Security type (Common Stock, etc.)
 */
data class SymbolLookupResult(
    val description: String = "",
    val displaySymbol: String = "",
    val symbol: String = "",
    val type: String = ""
)

/**
 * Symbol search response from Finnhub API.
 * @property count Number of results
 * @property results List of matching symbols
 */
data class SymbolLookup(
    val count: Int = 0,
    @SerializedName("result")
    val results: List<SymbolLookupResult> = emptyList()
)