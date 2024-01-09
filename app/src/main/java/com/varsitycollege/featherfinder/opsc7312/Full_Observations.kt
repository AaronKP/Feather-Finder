package com.varsitycollege.featherfinder.opsc7312

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Full_Observations : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_observations)

        recyclerView = findViewById(R.id.recyclerview)

        // Set a LinearLayoutManager to the RecyclerView
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val user = FirebaseAuth.getInstance().currentUser
        val database = FirebaseDatabase.getInstance()
        val databaseReference = database.getReference("observation")  // Replace "observation" with your Realtime Database node

        val myEmail = user?.email

        databaseReference.orderByChild("user").equalTo(myEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val observationsList = ArrayList<Data.Observation>()
                    for (dataSnapshot in dataSnapshot.children) {
                        val theObservation = dataSnapshot.getValue(Data.Observation::class.java)
                        if (theObservation != null) {
                            theObservation.documentID =
                                dataSnapshot.key.toString() // Access the unique key as the document ID
                            observationsList.add(theObservation)
                        }
                    }

                    if (observationsList.isEmpty()) {
                        // Handle the case when there are no observations with the specific email.
                        // You can display a message to the user or take appropriate action.
                    } else {
                        observationsList.reverse()
                        recyclerView.adapter = fullobsadapter(observationsList)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@Full_Observations, databaseError.toString(), Toast.LENGTH_SHORT).show()
                }
            })


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