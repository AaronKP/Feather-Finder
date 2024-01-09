package com.varsitycollege.featherfinder.opsc7312

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DataBird(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double
): Serializable
