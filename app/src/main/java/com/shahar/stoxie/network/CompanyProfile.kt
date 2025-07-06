package com.shahar.stoxie.network

import com.google.gson.annotations.SerializedName

/**
 * Represents the company profile data from the Finnhub API.
 * We are primarily interested in the 'finnhubIndustry' field for the sector chart.
 */
data class CompanyProfile(

    @SerializedName("name")
    val name: String = "",
    @SerializedName("finnhubIndustry")
    val sector: String = "N/A" // Default to "N/A" if the sector is not available
)