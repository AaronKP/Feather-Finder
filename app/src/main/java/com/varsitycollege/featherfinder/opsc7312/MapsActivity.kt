package com.varsitycollege.featherfinder.opsc7312

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.media.Image
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
//import com.example.mynewmaps.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.varsitycollege.featherfinder.opsc7312.databinding.ActivityMapsBinding
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import android.speech.tts.TextToSpeech
import android.text.Html
import java.util.Locale
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.model.LatLng as GoogleMapsLatLng
import com.google.maps.android.PolyUtil
import com.google.maps.model.*
import java.text.SimpleDateFormat

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, TextToSpeech.OnInitListener{

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var lastLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val apiKey = "6tg0n9esfnme"

    private var directionsSteps: List<DirectionsStep>? = null
    private var textToSpeech: TextToSpeech? = null
    //private lateinit var bottomSheetBehavior: BottomSheetBehavior<NestedScrollView>
    //HOTSPOT RADIUS
    private lateinit var distanceSpinner: Spinner
    var THEDIRECTIONSTEXT: String =""

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Retrieve location data from the intent
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        if (latitude != 0.0 && longitude != 0.0) {
            val currentLatLng = LatLng(latitude, longitude)
            placeMarkerOnMap(currentLatLng)
        }

        //HOTSPOT RADIUS CODE
        // Find the Spinner component
        distanceSpinner = findViewById(R.id.distanceSpinner)

        textToSpeech = TextToSpeech(this, this)
// Create an array with distances for the spinner
        val distances = resources.getStringArray(R.array.range_options).toMutableList()
        distances.add(0, "Select Distance Radius") // Add a hint for the spinner

// Set up the Spinner with the updated array
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, distances)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        distanceSpinner.adapter = adapter

// Set the default selection to the hint
        distanceSpinner.setSelection(0)

// Set up the Spinner item selection listener
        distanceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val selectedDistanceString = parent?.getItemAtPosition(position) as String
                    val selectedDistance = selectedDistanceString.split(" ")[0].toDoubleOrNull()
                    if (selectedDistance != null) {
                        showHotspotWithinRadius(lastLocation, selectedDistance.toString())
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle when no item is selected
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        Location("")
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)

        // Retrieve user's email from FirebaseAuth
        val user = FirebaseAuth.getInstance().currentUser
        val myEmail = user?.email

        // Get reference to the "observation" node in the database
        val databaseReference = FirebaseDatabase.getInstance().getReference("observation")

        // Retrieve observations from the database based on user's email
        databaseReference.orderByChild("user").equalTo(myEmail).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val observationsList = ArrayList<Data.Observation>()
                for (dataSnapshot in dataSnapshot.children) {
                    val theObservation = dataSnapshot.getValue(Data.Observation::class.java)
                    if (theObservation != null) {
                        theObservation.documentID = dataSnapshot.key.toString()
                        observationsList.add(theObservation)
                    }
                }

                if (observationsList.isEmpty()) {
                    // Handle the case when there are no observations with the specific email.
                    // You can display a message to the user or take appropriate action.
                } else {
                    observationsList.reverse()
                    // Loop through observationsList and add markers on the map
                    for (observation in observationsList) {
                        val latString = observation.lat
                        val longString = observation.long
                        if (latString.isNotEmpty() && longString.isNotEmpty()) {
                            val yellowMarkerLatLng = LatLng(latString.toDouble(), longString.toDouble())
                            addYellowMarker(yellowMarkerLatLng, observation)
                        } else {
                            Log.e("MapsActivity", "Invalid lat or long in observation, ${observation.sciName}")
                        }
                    }

                    // If you want to focus the camera on the last observation, you can do it here
                    if (observationsList.isNotEmpty()) {
                        val lastObservation = observationsList.first()
                        val lastObservationLatLng = LatLng(lastObservation.lat.toDouble(), lastObservation.long.toDouble())
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastObservationLatLng, 12f))
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@MapsActivity, databaseError.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        SetUpMap()
    }



    private fun addYellowMarker(latLng: LatLng, observationData: Data.Observation) {
        val marker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Observations")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
        )
        // Set a custom tag to identify yellow markers
        marker.tag = observationData
    }

    private fun SetUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                //val namedLocation = Data.NamedLocation(location, "Custom Location Name")
                val currentLatLog = LatLng(location.latitude, location.longitude)
                fetchHotspots(location.latitude, location.longitude)
                //placeMarkerOnMap(currentLatLog)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLog, 12f))
            }
        }
    }
    private fun placeMarkerOnMap(currentLatLng: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLng)
        markerOptions.title("$currentLatLng")
        mMap.addMarker(markerOptions)
    }
    private fun addMarker(latLng: LatLng, title: String) {
        mMap.addMarker(MarkerOptions().position(latLng).title(title))
    }
    //override fun onMarkerClick(p0: Marker) = true
    private var hotspotss: List<HotspotLocationData>? = null

    private fun fetchHotspots(latitude: Double, longitude: Double) {
        val customBaseUrl = "https://api.ebird.org/v2/"
        val retrofit = Retrofit.Builder()
            .baseUrl(customBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val hotspotApiService = retrofit.create(HotspotService::class.java)
        val call: Call<List<HotspotLocationData>> = hotspotApiService.getHotspots(apiKey, latitude, longitude)
        call.enqueue(object : Callback<List<HotspotLocationData>> {
            override fun onResponse(call: Call<List<HotspotLocationData>>, response: Response<List<HotspotLocationData>>) {
                if (response.isSuccessful) {
                    val hotspots = response.body()
                    if (hotspots != null && hotspots.isNotEmpty()) {
                        hotspotss = hotspots // Update the hotspotss variable here
                        for (hotspot in hotspots) {
                            val hotspotLatLng = LatLng(hotspot.lat, hotspot.lng)
                            // Set the title of the marker
                            Log.i("Debug", "name: ${hotspot.id}, (${hotspot.lat}, ${hotspot.lng})")
                            placeMarkerOnMap(hotspotLatLng)
                        }
                        // If you want to focus the camera on the last hotspot, you can do it here
                        if (hotspots.isNotEmpty()) {
                            val lastHotspot = hotspots.last()
                            val lastHotspotLatLng = LatLng(lastHotspot.lat, lastHotspot.lng)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastHotspotLatLng, 12f))
                        }
                    }
                } else {
                    // Handle unsuccessful response (e.g., log an error message)
                    Log.e("HotspotTracker", "Failed to retrieve hotspots")
                }
            }
            override fun onFailure(call: Call<List<HotspotLocationData>>, t: Throwable) {
                Log.e("HotspotTracker", "API call failed", t)
            }
        })
    }

    private fun showHotspotWithinRadius(centerLocation: Location?, selectedDistanceString: String) {
        if (centerLocation != null) {
            val centerLatLng = LatLng(centerLocation.latitude, centerLocation.longitude)
            val selectedDistance = selectedDistanceString.split(" ")[0].toDoubleOrNull()
            if (selectedDistance != null) {
                // Clear all markers on the map
                mMap.clear()
                // Add the user's location marker
                placeMarkerOnMap(centerLatLng)
                val filteredHotspots = hotspotss?.filter { hotspot ->
                    val hotspotLatLng = LatLng(hotspot.lat, hotspot.lng)
                    val distance = calculateDistance(lastLocation, hotspotLatLng)
                    distance <= selectedDistanceString
                }
                filteredHotspots?.forEach { hotspot ->
                    val hotspotLatLng = LatLng(hotspot.lat, hotspot.lng)
                    placeMarkerOnMap(hotspotLatLng)
                }
                // Update the circle radius to the selected distance
                val circleOptions = CircleOptions()
                    .center(centerLatLng)
                    .radius(selectedDistance * 1000) // Convert km to meters
                    .strokeWidth(3f)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.parseColor("#500084d3")) // Semi-transparent blue
                mMap.addCircle(circleOptions)
            }
        } else {
            // Handle the case where the location is null
        }
    }
    private val googleMapsApiKey = "AIzaSyDI8N7HDSNKesVFs6wT5LE60AZW4qpw6ck"
    // Initialize a GeoApiContext
    private val geoApiContext = GeoApiContext.Builder().apiKey(googleMapsApiKey).build()
    // Add this function to your activity
    // Add a class-level variable to store the current polyline
    private var currentPolyline: Polyline? = null
    // Update the showDirectionsToMarker function
    // Update the showDirectionsToMarker function
    private fun showDirectionsToMarker(destination: LatLng) {
        val origin = LatLng(lastLocation?.latitude ?: 0.0, lastLocation?.longitude ?: 0.0)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val directionsResult: DirectionsResult = DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.DRIVING)
                    .origin("${origin.latitude},${origin.longitude}")
                    .destination("${destination.latitude},${destination.longitude}")
                    .await()
                val points = directionsResult.routes[0].overviewPolyline.decodePath()
                directionsSteps = directionsResult.routes[0].legs.flatMap { it.steps.asIterable() }
                val polylineOptions = PolylineOptions()
                for (point in points) {
                    polylineOptions.add(LatLng(point.lat, point.lng))
                }
                polylineOptions.color(Color.RED)

                // Add the new polyline to your map on the main thread
                launch(Dispatchers.Main) {
                    currentPolyline = mMap.addPolyline(polylineOptions)


                    // Adjust the camera position to focus on the drawn route
                    val bounds = LatLngBounds.Builder().include(origin).include(destination)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))

                    drawPolyline(directionsResult.routes[0])
                    //displayDirectionsOnMap(directionsResult)
                    updateInstructions()

                    val directions = directionsResult.routes[0].legs[0].steps
// Create a StringBuilder to build the directions text


                    val directionsText = StringBuilder()
                    val sharedPref = getSharedPreferences("Directions", Context.MODE_PRIVATE)
                    for (step in directions) {
                        val formattedStep =
                            Html.fromHtml(step.htmlInstructions, Html.FROM_HTML_MODE_COMPACT)
                                .toString()
                        directionsText.append(formattedStep).append("\n")
                    }
                    THEDIRECTIONSTEXT = directionsText.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        // Release TextToSpeech when the activity is destroyed
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onDestroy()
    }

    // Implement the onInit method for TextToSpeech
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set the language to the device's default language
            val result = textToSpeech?.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TextToSpeech", "Language not supported")
            }
        } else {
            Log.e("TextToSpeech", "Initialization failed")
        }
    }
    // ...
    private fun displayDirectionsOnMap(result: DirectionsResult?) {
        if (result != null && result.routes.isNotEmpty()) {
            val route = result.routes[0]
            val steps = route.legs.flatMap { it.steps.asIterable() }
            // Draw the polyline on the map
            drawPolyline(route)
            // Display turn-by-turn instructions
            for (step in steps) {
                val instruction = step.htmlInstructions
                speak(instruction) // Make sure the speak function is defined
                // You can also display the instruction on the UI or use it as needed
            }
        }
    }

    private fun drawPolyline(route: DirectionsRoute) {
        val decodedPath = PolyUtil.decode(route.overviewPolyline.encodedPath)
        val polylineOptions = PolylineOptions().addAll(decodedPath)
        polylineOptions.color(Color.RED)
        // Add the new polyline to your map on the main thread
        runOnUiThread {
            currentPolyline?.remove() // Remove previous polyline
            currentPolyline = mMap.addPolyline(polylineOptions)
            // Adjust the camera position to focus on the drawn route
            val boundsBuilder = LatLngBounds.builder()
            for (point in decodedPath) {
                boundsBuilder.include(point)
            }
            val bounds = boundsBuilder.build()
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }
    private fun updateInstructions() {
        val currentLocation = LatLng(lastLocation?.latitude ?: 0.0, lastLocation?.longitude ?: 0.0)
        val currentStep = getCurrentStep(currentLocation)

        currentStep?.let { step ->
            val instruction = stripHtmlTags(step.htmlInstructions)
            speak(instruction)
            // You can also display the instruction on the UI or use it as needed
        } ?: run {
            // Handle case when there is no valid step (user deviated from the path)
            speak("You are off the route. Recalculating directions.")
            // You may want to trigger a route recalculation or guide the user back to the route
        }
    }

    private fun stripHtmlTags(htmlString: String): String {
        // Remove HTML tags using a simple regex
        return htmlString.replace(Regex("<.*?>"), "")
    }

    // Function to get the current step based on the user's location
    private fun getCurrentStep(currentLocation: LatLng): DirectionsStep? {
        return directionsSteps?.find { step ->
            val decodedPath = PolyUtil.decode(step.polyline.encodedPath)
            PolyUtil.isLocationOnPath(currentLocation, decodedPath, true, 50.0)
        }
    }
    private fun getFirstInstructionStep(): String? {
        // Assuming that directionsSteps is a list of DirectionsStep objects
        val firstStep = directionsSteps?.getOrNull(0)
        return firstStep?.htmlInstructions?.let { stripHtmlTags(it) }
    }

    private fun showBottomSheetDialog(locationName: String, distance: String, latLng: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.bottomsheetlayout)

        val locationNameTextView = dialog.findViewById<TextView>(R.id.name)
        val distanceTextView = dialog.findViewById<TextView>(R.id.distance)
        val latLngTextView = dialog.findViewById<TextView>(R.id.coordinates)
        val startBtn = dialog.findViewById<Button>(R.id.startBtn)

        locationNameTextView.text = "$locationName "
        distanceTextView.text = "$distance "
        latLngTextView.text = "$latLng "

        Log.d("Debug", ": $locationName")
        Log.d("Debug", ": $distance")
        Log.d("Debug", ": $latLng")

        startBtn.setOnClickListener {
            // Create and show the dialog box
            val alertDialog = AlertDialog.Builder(this@MapsActivity)
                .setTitle("Directions")
                .setMessage(THEDIRECTIONSTEXT)
                .setPositiveButton("OK") { dialog, _ ->
                    // Handle positive button click
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // Handle negative button click
                    dialog.dismiss()
                }
                .create()

            alertDialog.show()
        }
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }


    private fun showObservationBottomSheetDialog(observationData: Data.Observation) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.observationbottomsheet)

        val Commonnametv = dialog.findViewById<TextView>(R.id.myComNametxt)
        val scinametv = dialog.findViewById<TextView>(R.id.mySciNametxt)
        val thelocationtv = dialog.findViewById<TextView>(R.id.myLocationtxt)
        val thedatetv = dialog.findViewById<TextView>(R.id.myDatetxt)
        val thetimetv = dialog.findViewById<TextView>(R.id.myTimetxt)

        Commonnametv.text = "${observationData.comName}"
        scinametv.text = "${observationData.sciName}"
        thedatetv.text = "${formatDate(observationData.date)}"
        thetimetv.text = "${observationData.time}"
        thelocationtv.text = "${observationData.location}"


        // Load and display the image using Glide
        val imageView = dialog.findViewById<CircleImageView>(R.id.obsimage)


        // Use Glide to load the image from Firebase Storage
        Glide.with(imageView.context)
            .load(observationData.imageUrl)
            .placeholder(R.drawable.birddefault) // You can use a placeholder image
            .error(R.drawable.birddefault) // You can use an error image
            .into(imageView)

        val detailsbtn = dialog.findViewById<Button>(R.id.detailsbtn)
        detailsbtn.setOnClickListener(){
            val intent = Intent(this, ObservationDetail::class.java)
            intent.putExtra("observationData", observationData)  // Pass the serialized observation instance
            this.startActivity(intent)
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }
    // Add a class-level variable to store the currently selected marker
    private var currentlySelectedMarker: Marker? = null
    //private var currentPolyline: Polyline? = null
    // Update the onMarkerClick function
    override fun onMarkerClick(marker: Marker): Boolean {
        // Clear the previous route if there was one
        clearPreviousRoute()

        val isYellowMarker = marker.tag is Data.Observation
        if (isYellowMarker) {
            // Retrieve the observation data from the marker's tag
            val observationData = marker.tag as Data.Observation

            // Perform your action for the clicked yellow marker here
            showObservationBottomSheetDialog(observationData)
        } else {

            val clickedHotspot =
                hotspotss?.find { it.lat == marker.position.latitude && it.lng == marker.position.longitude }
            if (clickedHotspot != null) {

                val destinationLatLng = LatLng(clickedHotspot.lat, clickedHotspot.lng)
                // Display the new route
                showDirectionsToMarker(destinationLatLng)
                // Display the bottom sheet dialog
                val name = reverseGeocode(destinationLatLng, marker)
//                    showBottomSheetDialog(
//                        locationName = name.toString(),
//                        distance = calculateDistance(lastLocation, destinationLatLng),
//                        latLng = destinationLatLng.toString()
//                    )
                showBottomSheetDialog(locationName = name.toString(), distance = calculateDistance(lastLocation, destinationLatLng), latLng = getFirstInstructionStep().toString())
                // Update the currently selected marker
                currentlySelectedMarker = marker
            }
        }
        return true
    }
    // Function to clear the previous route
    private fun clearPreviousRoute() {
        currentPolyline?.remove()
        currentlySelectedMarker = null
    }

    fun formatDate(inputDate: String): String {
        // Define the input and output date formats
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        try {
            // Parse the input date
            val parsedDate = inputFormat.parse(inputDate)

            // Format the parsed date to the desired output format
            return outputFormat.format(parsedDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Return the original input if there is an error
        return inputDate
    }

    private fun reverseGeocode(location: LatLng, marker: Marker): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        if (addresses != null) {
            if (addresses.isNotEmpty()) {
                val address = addresses?.get(0)
                val locationName = address?.getAddressLine(0)
                marker.title = locationName
                marker.showInfoWindow()
                Log.d("Debug", "Hotspot count: $locationName")
                return  locationName.toString()
            } else {
                // Handle the case where no location name is found
                marker.title = "Unknown Location"
                marker.showInfoWindow()
                return marker.title.toString()
            }
        }
        return addresses.toString()
    }

    private fun calculateDistance(startLocation: Location?, endLatLng: LatLng): String {
        if (startLocation == null) return ""
        val startLatLng = LatLng(startLocation.latitude, startLocation.longitude)
        val earthRadius = 6371 // Radius of the Earth in kilometers
        val dLat = Math.toRadians(endLatLng.latitude - startLatLng.latitude)
        val dLng = Math.toRadians(endLatLng.longitude - startLatLng.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(startLatLng.latitude)) * Math.cos(Math.toRadians(endLatLng.latitude)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distanceInKm = earthRadius * c

        val sharedPreferences = getSharedPreferences("SwitchSettings", Context.MODE_PRIVATE)
        val km = sharedPreferences.getBoolean("isKmSwitchOn", false)
        val miles = sharedPreferences.getBoolean("isMilesSwitchOn", false)

        return if (km) {
            String.format("%.2f km", distanceInKm)
        } else if (miles) {
            val milesConversionFactor = 0.621371 // 1 kilometer is approximately 0.621371 miles
            val distanceInMiles = distanceInKm * milesConversionFactor
            String.format("%.2f miles", distanceInMiles)
        } else {
            // If neither km nor miles flag is set, return distance in kilometers by default
            String.format("%.2f km", distanceInKm)
        }
    }
    // Function to calculate and display directions
}