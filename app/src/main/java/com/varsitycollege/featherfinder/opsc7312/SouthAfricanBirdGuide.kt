package com.varsitycollege.featherfinder.opsc7312

import FavouriteGesture
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
//api
import kotlin.collections.ArrayList
import com.opencsv.CSVReader
import kotlinx.coroutines.*
import java.io.StringReader

//database code
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class SouthAfricanBirdGuide : AppCompatActivity() {
    ///TODO ADD TO FAVOURITES FEATURE
    //implement recycler view
    private lateinit var newBirdGuideRecyclerView : RecyclerView
    //array list for bird objects
    private lateinit var newBirdAArrayList: ArrayList<Data.BirdSpecies>

    //arraylist for favourited guides
    private lateinit var favouriteList: ArrayList<Data.BirdSpecies>

    //arraylist for filtered/search data
    private lateinit var tempSearchList: ArrayList<Data.BirdSpecies>
    private lateinit var birdSearchView : SearchView

    //progress bar
    private lateinit var progressBar: ProgressBar // Declare the ProgressBar

    //arrays for images and names
    lateinit var imageID : Array<Int>
    lateinit var commonNames : Array<String>
    lateinit var scientificNames : Array<String>
    lateinit var spCodes: ArrayList<String>

    private lateinit var adaptRecV: MySpeciesAdapter

    //database code
    // Initialize Firebase Database reference
    private lateinit var databaseReference: DatabaseReference
    // Firebase Authentication
    private val auth = FirebaseAuth.getInstance()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_south_african_bird_guide)



        spCodes = ArrayList()
        // Initialize the TextView for the identificationText

        birdSearchView =findViewById(R.id.searchViewGuide)



        newBirdGuideRecyclerView = findViewById(R.id.birdGuideRecyclerV)
        newBirdGuideRecyclerView.layoutManager=LinearLayoutManager(this)
        newBirdGuideRecyclerView.setHasFixedSize(true)

        newBirdAArrayList = arrayListOf<Data.BirdSpecies>()

        //initialize bird search arraylist
        tempSearchList= arrayListOf<Data.BirdSpecies>()

        birdSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }
        })

        // Initialize the ProgressBar

        progressBar = findViewById(R.id.loadingProgressBar)

        // Make the ProgressBar visible
        progressBar.visibility = View.VISIBLE

        adaptRecV = MySpeciesAdapter(newBirdAArrayList)

        CoroutineScope(Dispatchers.IO).launch {
            try {

                // Data fetching process
                fetchData()

            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions appropriately
            } finally {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE // Hide the ProgressBar
                    adaptRecV.setData(newBirdAArrayList)
                    getUserData()
                }
            }

        }


        //database favourites
        databaseReference = FirebaseDatabase.getInstance().reference.child("favorites")


    }

    private suspend fun fetchData() {
        val apiUrl = "https://api.ebird.org/v2/product/spplist/SA?key=6tg0n9esfnme"
        val speciesCodes: List<String> = coroutineScope {
            val url = URL(apiUrl)
            val data = url.readText()
            val jsonArray = JSONArray(data.trimIndent())
            (0 until jsonArray.length()).mapTo(ArrayList()) { jsonArray.getString(it) }
        }

        // Use async for parallel API calls
        val birdSpeciesDeferred = speciesCodes.map { speciesCode ->
            CoroutineScope(Dispatchers.IO).async { // Use CoroutineScope(Dispatchers.IO).async instead of async(Dispatchers.IO)
                val speciesDataUrl = "https://api.ebird.org/v2/ref/taxonomy/ebird?species=$speciesCode&key=6tg0n9esfnme"
                val speciesUrl = URL(speciesDataUrl)
                val speciesData = speciesUrl.readText()
                val fields = speciesData.split(',')
                val scientificName = if (fields[14].contains("FAMILY_CODE")) {
                    fields[14].substringAfter("\n")
                } else {
                    fields[14]
                }
                val commonName = fields[15]
                val order = fields[22]
                val famCom = fields[23]
                val famSci = fields[24]
                Data.BirdSpecies(commonName, scientificName, speciesCode, order, famCom, famSci)
            }
        }

        newBirdAArrayList = birdSpeciesDeferred.awaitAll() as ArrayList<Data.BirdSpecies>
    }




    private fun getUserData() {

        //code for returning com name and sci name from api
        runBlocking {
            launch(Dispatchers.IO) {
                try {
                    for (i in 0..5) {
                        val code = spCodes[i]
                        val apiUrl = "https://api.ebird.org/v2/ref/taxonomy/ebird?species=$code&key=6tg0n9esfnme"
                        val url = URL(apiUrl)
                        val urlConnection = url.openConnection() as HttpURLConnection
                        try {
                            val data = url.readText()

                            // Parsing the data as a simple string
                            val fields = data.split(',')

                            // Extracting the required fields
                            val scientificName = if (fields[14].contains("FAMILY_CODE")) {
                                fields[14].substringAfter("\n")
                            } else {
                                fields[14]
                            }
                            val commonName = fields[15]
                            val speciesCode = fields[16].trim()
                            val order =fields[22]
                            val famCom = fields[23]
                            val famSci =fields[24]

                            val birdGuide = Data.BirdSpecies(commonName, scientificName, speciesCode,order,famCom,famSci)
                            if (!newBirdAArrayList.contains(birdGuide)) {
                                newBirdAArrayList.add(birdGuide)
                            }


                            adaptRecV.setData(newBirdAArrayList)


                        }finally {
                            urlConnection.disconnect()
                        }

                        // Hide the ProgressBar once the data is populated in the RecyclerView
                        progressBar.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle any exceptions appropriately
                }
            }

        }

        //loop through arrays and create object of the class to store it in newBirdAArrayList
        /*for(i in imageID.indices){
            val birdGuide = BirdSpecies(imageID[i],commonNames[i],scientificNames[i])
            newBirdAArrayList.add(birdGuide)
        }*/

        //add all data that has been added to the new array list into the temp search list
        tempSearchList.addAll(newBirdAArrayList)

        adaptRecV = MySpeciesAdapter(tempSearchList)

        //database favourites
        databaseReference = FirebaseDatabase.getInstance().reference.child("favorites")

        //gesture code
        val swipeGesture = object : FavouriteGesture(this, databaseReference) {
//
        }

        // For swipe gesture (favourite)
        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(newBirdGuideRecyclerView)


        //on bird click code
        newBirdGuideRecyclerView.adapter=adaptRecV
        adaptRecV.setOnBirdClick(object :MySpeciesAdapter.I_onBirdClickListener{
            //parameter is the position the user clicked
            override fun onBirdClick(position: Int) {
                val clickedBird = tempSearchList[position] // Get the clicked item from tempSearchList

                val originalPosition = newBirdAArrayList.indexOf(clickedBird) // Find the original position of the clicked item in the complete list

                val intent = Intent(this@SouthAfricanBirdGuide, Guide::class.java)
                intent.putExtra("clickedBird", newBirdAArrayList[originalPosition]) // Pass the clickedBird to the Guide activity using the original position
                startActivity(intent)

            }

        })
    }

    //method to filter/search the bird list
    private fun filter(query: String?) {
        tempSearchList.clear() // Clear the current search results

        if (query.isNullOrBlank()) {
            tempSearchList.addAll(newBirdAArrayList) // If query is empty, show all items
        } else {
            val filteredList = newBirdAArrayList.filter { bird ->
                bird.commonName.contains(query, ignoreCase = true) ||
                        bird.scientificName.contains(query, ignoreCase = true)
            }
            tempSearchList.addAll(filteredList)
        }


        newBirdGuideRecyclerView.adapter?.notifyDataSetChanged() // Notify adapter of data change

    }
}