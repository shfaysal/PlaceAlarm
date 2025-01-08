package com.example.placealarm.remote

import com.example.placealarm.models.DirectionsResponse
import com.google.android.gms.maps.model.LatLng
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionApiService {

    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") origin : String,
        @Query("destination") destination : String,
        @Query("key") apiKey : String
    ): DirectionsResponse

}