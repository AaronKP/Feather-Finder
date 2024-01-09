package com.varsitycollege.featherfinder.opsc7312

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.AppCompatImageButton
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.integrity.i
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import okhttp3.*
import org.json.JSONObject
import java.util.*
import java.io.IOException
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // the lateint means we will inti later

    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navView: NavigationView
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    //bird of the day
    private lateinit var birdImage: de.hdodenhof.circleimageview.CircleImageView
    private lateinit var commonNameTextView: TextView
    private lateinit var scientificNameTextView: TextView


    private lateinit var recyclerView: RecyclerView
    private lateinit var observationslist: ArrayList<Data.Observation>
    private var db = Firebase.firestore
    // Declare database as a class-level variable
    private lateinit var database: FirebaseDatabase


    companion object {
        private const val REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()
        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance()

        recyclerView = findViewById(R.id.recyclerview) // Initialize the recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Initialize observationslist with an empty ArrayList
        observationslist = ArrayList()

        val btn = findViewById<Button>(R.id.allObsBtn)

        btn.setOnClickListener() {
            val intent = Intent(this, Full_Observations::class.java)
            startActivity(intent)
        }

        // Inflate the custom layout for the ActionBar
        val actionBar = supportActionBar
        actionBar?.setDisplayShowCustomEnabled(true)
        actionBar?.setDisplayShowTitleEnabled(false) // Hide the default title
        actionBar?.setCustomView(R.layout.action_bar_custom_layout)

        // You can access the ImageView within the custom layout and further customize it if needed
        val logoImageView: ImageView =
            actionBar?.customView?.findViewById(R.id.action_bar_logo) ?: return

        // Get the LinearLayout from the custom layout and set its gravity to center
        val customActionBarLayout = actionBar.customView?.parent as? LinearLayout
        customActionBarLayout?.gravity = Gravity.CENTER

        // Optionally, you can adjust the ImageView's layout parameters to fill the available space
        val layoutParams = logoImageView.layoutParams as? LinearLayout.LayoutParams
        layoutParams?.width = LinearLayout.LayoutParams.MATCH_PARENT
        layoutParams?.height = LinearLayout.LayoutParams.MATCH_PARENT
        logoImageView.layoutParams = layoutParams

        navView = findViewById(R.id.navView)
        navView.setNavigationItemSelectedListener(this)

        // Set the menu to the NavigationView
        navView.menu.clear()
        navView.inflateMenu(R.menu.nav_header_menu)

        val drawerLayout: DrawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set the color of the hamburger icon
        toggle.drawerArrowDrawable?.setColorFilter(
            resources.getColor(R.color.black),
            PorterDuff.Mode.SRC_ATOP
        )

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        //actual page code
        // Retrieve the user's display name from Firebase Authentication
        val user = FirebaseAuth.getInstance().currentUser
        val displayName = user?.displayName

        val greeting = findViewById<TextView>(R.id.greetingtxt)
        // Get the current time
        val currentTime = Calendar.getInstance()
        val hourOfDay = currentTime.get(Calendar.HOUR_OF_DAY)

        // Determine the greeting message based on the time
        val greetingMessage = when {
            hourOfDay >= 0 && hourOfDay < 12 -> "Good Morning"
            hourOfDay >= 12 && hourOfDay < 18 -> "Good Afternoon"
            else -> "Good Day"
        }
        greeting.text = "$greetingMessage, $displayName"

        //navbar header code
        // Set email value to TextView in nav_header.xml
        val headerView = navView.getHeaderView(0)
        val emailTextView: TextView = headerView.findViewById(R.id.emailtxt)
        emailTextView.text = user?.email
        // Set name value to TextView in nav_header.xml
        val nameView: TextView = headerView.findViewById(R.id.user_name)
        nameView.text = user?.displayName

        // Retrieve the user's profile image URL from the database
        val usersRef = database.getReference("users")

        user?.uid?.let {
            usersRef.child(it).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String::class.java)

                    // Assume profilePictureUrl is the URL of the user's profile picture
                    val profileImageView: de.hdodenhof.circleimageview.CircleImageView = headerView.findViewById(R.id.profile_image)

                    // Load the profile image using Picasso
                    if (!profileImageUrl.isNullOrBlank()) {
                        Picasso.get().load(profileImageUrl).into(profileImageView)
                    } else {
                        // If the profile picture URL is not available, you can set a default image or handle it accordingly
                        profileImageView.setImageResource(R.drawable.profile)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle the error if needed
                }
            })
        }



        val database = FirebaseDatabase.getInstance()
        val databaseReference = database.getReference("observation")  // Replace "observation" with your Realtime Database node

        val myEmail = user?.email

        databaseReference.orderByChild("user")
            .equalTo(myEmail)
            .limitToLast(3)  // Limit the result to the last 3 observations
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val observationsList = ArrayList<Data.Observation>()

                    for (dataSnapshot in dataSnapshot.children) {
                        val theObservation = dataSnapshot.getValue(Data.Observation::class.java)
                        if (theObservation != null) {
                            theObservation.documentID = dataSnapshot.key.toString() // Access the unique key as the document ID
                            observationsList.add(theObservation)
                        }
                    }

                    if (observationsList.isEmpty()) {
                        // Handle the case when there are no observations with the specific email.
                        // You can display a message to the user or take appropriate action.
                    } else {
                        observationsList.reverse()
                        recyclerView.adapter = ObservationAdapter(observationsList)

                        // Save observationsList to SharedPreferences
                        saveObservationsToSharedPreferences(observationsList)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@MainActivity, databaseError.toString(), Toast.LENGTH_SHORT).show()
                }
            })













        val tablemountain: CardView = findViewById(R.id.tablemountainwv)

// Set an OnClickListener for the CardView
        tablemountain.setOnClickListener {
            // Handle the click event here, for example, start a new activity
            val intent = Intent(this, BirdGuideLocation::class.java)
            val code = "TableMountain"
            intent.putExtra("code", code)  // Change "extraString" to "code"
            startActivity(intent)
        }


        val karoo: CardView = findViewById(R.id.karoowv)

// Set an OnClickListener for the CardView
        karoo.setOnClickListener {
            // Handle the click event here, for example, start a new activity
            val intent = Intent(this, BirdGuideLocation::class.java)
            val code = "karoo"
            intent.putExtra("code", code)  // Change "extraString" to "code"
            startActivity(intent)
        }


        val vaalbos: CardView = findViewById(R.id.vaalboswv)

// Set an OnClickListener for the CardView
        vaalbos.setOnClickListener {
            // Handle the click event here, for example, start a new activity
            val intent = Intent(this, BirdGuideLocation::class.java)
            val code = "vaalbos"
            intent.putExtra("code", code)  // Change "extraString" to "code"
            startActivity(intent)
        }

        val krugernational: CardView = findViewById(R.id.krugernationalwv)

// Set an OnClickListener for the CardView
        krugernational.setOnClickListener {
            // Handle the click event here, for example, start a new activity
            val intent = Intent(this, BirdGuideLocation::class.java)
            val code = "krugernational"
            intent.putExtra("code", code)  // Change "extraString" to "code"
            startActivity(intent)
        }


        val seemoreguidesbtn: Button = findViewById(R.id.seemoreguidesbtn)

// Set an OnClickListener for the CardView
        seemoreguidesbtn.setOnClickListener {
            // Handle the click event here, for example, start a new activity
            val intent = Intent(this, BirdGuideLocation::class.java)
            startActivity(intent)
        }


        val randombird: CardView = findViewById(R.id.randombirdcv)

// Set an OnClickListener for the CardView
        randombird.setOnClickListener {
            // Handle the click event here, for example, start a new activity
            val intent = Intent(this, randombirdgenerator::class.java)
            startActivity(intent)
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        //bird guide search button (Home page)
        val homeSearch = findViewById<Button>(R.id.editTextTextPersonName)
        homeSearch.setOnClickListener {
            // Define the intent
            val intent = Intent(this,SouthAfricanBirdGuide::class.java
            )

            // Start the activity using the intent
            startActivity(intent)
        }




// Assume profilePictureUrl is the URL of the user's profile picture
        val profilePictureUrl = user?.photoUrl?.toString()


        val profileImageView: de.hdodenhof.circleimageview.CircleImageView = headerView.findViewById(R.id.profile_image)

// Use Picasso to load the image into the CircleImageView
        if (!profilePictureUrl.isNullOrBlank()) {
            Picasso.get().load(profilePictureUrl).into(profileImageView)
        } else {
            // If the profile picture URL is not available, you can set a default image or handle it accordingly
            profileImageView.setImageResource(R.drawable.profile)
        }



    }




    private fun saveObservationsToSharedPreferences(observationsList: List<Data.Observation>) {
        val gson = Gson()
        val json = gson.toJson(observationsList)

        val sharedPreferences = getSharedPreferences("ObservationsData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("observationsList", json)
        editor.apply()
    }



    private fun getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        try {
                            val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            val intent = Intent(this@MainActivity, MapsActivity::class.java)
                            startActivity(intent)

                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
        } else {
            askPermission()
        }
    }
    private fun askPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                Toast.makeText(this@MainActivity, "Please provide the required permission", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home_item -> {
                // Handle item1 click
                val intent1 = Intent(this, MainActivity::class.java)
                startActivity(intent1)
            }
            R.id.map_item -> {
                getLastLocation()
            }
            R.id.recentobs_item -> {
                val intent4 = Intent(this, Full_Observations::class.java)
                startActivity(intent4)
            }
            R.id.favs_item -> {
                // Handle item5 click
                val intent5 = Intent(this, MyFavouriteBirds::class.java)
                startActivity(intent5)
            }
            R.id.settings_items -> {
                // Handle item6 click
                val intent6 = Intent(this, Settings::class.java)
                startActivity(intent6)
            }
            R.id.logout_items -> {

                val confirmDialog = AlertDialog.Builder(this)
                confirmDialog.setTitle("Confirmation")
                confirmDialog.setMessage("Are you sure you want to Log Out?")

                confirmDialog.setPositiveButton("Yes") { dialog, which ->
                    // Save changes and display success message
                    val successDialog = AlertDialog.Builder(this)
                    successDialog.setTitle("Success")
                    successDialog.setMessage("Successfully Logged Out.")
                    successDialog.setPositiveButton("OK") { _, _ ->

                        Firebase.auth.signOut()
                        val intent7 = Intent(this, welcome::class.java)
                        startActivity(intent7)

                    }
                    successDialog.show()
                }

                confirmDialog.setNegativeButton("No") { dialog, which ->
                    // Perform any action if needed when the user cancels the operation
                }

                confirmDialog.show()

            }
            // Handle other menu items here

            // Return true to indicate that the item selection has been handled
            else -> return true
        }

        // Return false to indicate that the item selection has not been handled
        return false
    }
}
