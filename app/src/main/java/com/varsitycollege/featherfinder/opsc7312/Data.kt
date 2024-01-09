package com.varsitycollege.featherfinder.opsc7312

import com.google.firebase.auth.FirebaseUser
import java.io.Serializable
import java.sql.Time
import java.time.LocalTime
import java.util.Date

class Data {
    data class HotspotResponse(val hotspots: List<HotspotLocationData>)
    data class HotspotLocationData(val LocationName: String, val lat: Double, val long: Double)

    //data class used to supply images and bird names to recycler view layout. Create adapter to feed in information to recycler view
    data class BirdSpecies(
        var commonName: String = "",
        var scientificName: String = "",
        var speciesCode: String = "",
        var order: String = "",
        var famComName: String = "",
        var famSciName: String = ""
    ) : Serializable {
        // Default (no-argument) constructor for Firebase
        constructor() : this("", "", "", "", "", "")
    }


    data class Observation(
        var documentID: String="",
        val user: String? ="",
        val comName: String ="",
        val sciName: String ="",
        val speciesCode: String ="",
        val location: String ="",
        val lat: String="",
        val long:String="",
        val date: String ="",
        val time: String ="",
        val imageUrl: String=""
    )  : Serializable

}
