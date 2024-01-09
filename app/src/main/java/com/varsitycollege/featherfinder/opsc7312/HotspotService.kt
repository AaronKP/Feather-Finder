package com.varsitycollege.featherfinder.opsc7312

import com.varsitycollege.featherfinder.opsc7312.DataBird
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.Call

interface HotspotService {
    @GET("data/obs/geo/recent")
    fun getHotspots(
        @Header("X-eBirdApiToken") apiKey: String,
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double
    ): Call<List<HotspotLocationData>>
}


