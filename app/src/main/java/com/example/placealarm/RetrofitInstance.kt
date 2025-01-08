package com.example.placealarm

import com.example.placealarm.remote.DirectionApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

val httpClient = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .build()

fun createDirectionApiService() : DirectionApiService {
    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create(DirectionApiService::class.java)
}

const val BASE_URL = "https://maps.googleapis.com/maps/api/"