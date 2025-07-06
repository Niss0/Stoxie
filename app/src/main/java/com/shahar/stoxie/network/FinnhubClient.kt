package com.shahar.stoxie.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FinnhubClient {

    private const val BASE_URL = "https://finnhub.io/api/v1/"

    // Use 'lazy' to create the Retrofit instance only when it's first needed.
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Expose the ApiService to the rest of the app.
    val api: FinnhubApiService by lazy {
        retrofit.create(FinnhubApiService::class.java)
    }
}