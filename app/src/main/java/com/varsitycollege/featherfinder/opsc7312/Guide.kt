package com.varsitycollege.featherfinder.opsc7312

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.util.Log
import android.widget.Button
import com.google.android.material.bottomnavigation.BottomNavigationView


class Guide : AppCompatActivity() {

    //lateinit var spCodes: ArrayList<String>
    lateinit var orderTxt: TextView // Declare the identificationText TextView
    lateinit var famComTxt: TextView
    lateinit var sciFamTxt: TextView
    lateinit var comNameTxt: TextView
    lateinit var sciNameTxt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)



        //retrieve sent species codes from main
        val clickedBird = intent.getSerializableExtra("clickedBird") as Data.BirdSpecies

        //Log.d("GuideActivity", "spCodes: $spCodes")
        // Initialize the identificationText TextView
        comNameTxt = findViewById(R.id.commonName)
        sciNameTxt =findViewById(R.id.scientificName)
        orderTxt = findViewById(R.id.orderText)
        famComTxt = findViewById(R.id.famComText)
        sciFamTxt = findViewById(R.id.famSciText)

        comNameTxt.text = clickedBird.commonName
        sciNameTxt.text=clickedBird.scientificName
        orderTxt.text = clickedBird.order
        famComTxt.text = clickedBird.famComName
        sciFamTxt.text = clickedBird.famSciName

        // Set the text of the identificationText TextView to the contents of the spCodes list
        //orderTxt.text = spCodes.joinToString(", ")

        //guide to observations code
        val addObsButton = findViewById<Button>(R.id.addObsGuide)
        addObsButton.setOnClickListener {
            val intent = Intent(this, AddObservation::class.java)

            //send bird object that was clicked to observation details
            intent.putExtra("clickedBird", clickedBird)
            startActivity(intent)
        }

        //bottom nav
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.add_item -> {
                    val intent1 = Intent(this, SouthAfricanBirdGuide::class.java)
                    startActivity(intent1)
                    true
                }
                R.id.home_item -> {
                    // Handle item2 click
                    val intent2 = Intent(this, MainActivity::class.java)
                    startActivity(intent2)
                    true
                }
                R.id.map_item -> {
                    // Handle item3 click
                    val intent3 = Intent(this, MapsActivity::class.java)
                    startActivity(intent3)
                    true
                }
                else -> false
            }
        }

    }


}