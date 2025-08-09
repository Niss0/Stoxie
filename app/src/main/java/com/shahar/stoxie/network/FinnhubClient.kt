package com.shahar.stoxie.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit client for Finnhub API.
 * Singleton with lazy initialization for efficient resource usage.
 */
object FinnhubClient {

    private const val BASE_URL = "https://finnhub.io/api/v1/"

    /**
     * Retrofit instance with Gson converter for JSON parsing.
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * API service interface implementation.
     */
    val api: FinnhubApiService by lazy {
        retrofit.create(FinnhubApiService::class.java)
    }
}