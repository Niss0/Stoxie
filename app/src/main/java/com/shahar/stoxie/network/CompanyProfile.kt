package com.shahar.stoxie.network

import com.google.gson.annotations.SerializedName

/**
 * Company profile data from Finnhub API.
 * Contains detailed company information and financial metrics.
 */
data class CompanyProfile(
    /** Company's headquarters country */
    val country: String = "",
    /** Trading currency */
    val currency: String = "",
    /** Primary stock exchange */
    val exchange: String = "",
    /** Industry sector */
    @SerializedName("finnhubIndustry")
    val sector: String = "N/A",
    /** IPO date */
    val ipo: String = "",
    /** Company logo URL */
    val logo: String = "",
    /** Market capitalization */
    @SerializedName("marketCapitalization")
    val marketCap: Double = 0.0,
    /** Company name */
    val name: String = "",
    /** Contact phone number */
    val phone: String = "",
    /** Total shares outstanding */
    @SerializedName("shareOutstanding")
    val sharesOutstanding: Double = 0.0,
    /** Stock ticker symbol */
    val ticker: String = "",
    /** Company website URL */
    val weburl: String = ""
)