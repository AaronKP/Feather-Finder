package com.varsitycollege.featherfinder.opsc7312

import com.google.gson.annotations.SerializedName

data class BridResponse(
    @SerializedName("hotspots") val hotspots: List<HotspotLocationData>
)