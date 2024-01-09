package com.varsitycollege.featherfinder.opsc7312

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.varsitycollege.featherfinder.opsc7312.databinding.ActivityMyFavouriteBirdsBinding

class MyFavouriteBirds : AppCompatActivity(), FavoritesAdapter.OnItemClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var binding: ActivityMyFavouriteBirdsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyFavouriteBirdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            // Reference to the user-specific node
            databaseReference = FirebaseDatabase.getInstance().getReference("favorites").child(userId)

            favoritesAdapter = FavoritesAdapter(ArrayList(), this)

            // Set up RecyclerView using binding
            binding.myFavBirdRecyclerV.layoutManager = LinearLayoutManager(this)
            binding.myFavBirdRecyclerV.adapter = favoritesAdapter

            // Set up a listener for changes in the database
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("Database", "onDataChange called")
                    Log.d("Database", "UserId: $userId")
                    Log.d("Database", "Snapshot key: ${snapshot.key}")
                    Log.d("Database", "Snapshot value: ${snapshot.value}")
                    Log.d("Database", "Snapshot children count: ${snapshot.childrenCount}")

                    val favoritesList = ArrayList<Data.BirdSpecies>()
                    for (birdSnapshot in snapshot.children) {
                        Log.d("Database", "Processing birdSnapshot: $birdSnapshot")
                        val favoriteBird = birdSnapshot.getValue(Data.BirdSpecies::class.java)
                        if (favoriteBird != null) {
                            favoritesList.add(favoriteBird)
                        }
                    }
                    Log.d("Database", "Favorites List Size: ${favoritesList.size}")

                    favoritesAdapter.setData(favoritesList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Database", "Error fetching data", error.toException())
                }
            })
        }
    }

    // Implementation of the item click interface
    override fun onItemClick(birdSpecies: Data.BirdSpecies) {
        // Handle item click here
        val intent = Intent(this, Guide::class.java)
        intent.putExtra("clickedBird", birdSpecies)
        startActivity(intent)
    }

    // Implementation of the item long click interface
    override fun onItemLongClick(birdSpecies: Data.BirdSpecies) {
        // Show a confirmation dialog with custom theme
        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("Delete Bird")
            .setMessage("Are you sure you want to delete this bird from favorites?")
            .setPositiveButton(android.R.string.yes) { _, _ ->
                // User clicked Yes, perform the deletion
                deleteFavoriteBird(birdSpecies)
            }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }

    private fun deleteFavoriteBird(birdSpecies: Data.BirdSpecies) {
        // Delete the bird from the database based on commonName
        val commonName = birdSpecies.commonName
        val query = databaseReference.orderByChild("commonName").equalTo(commonName)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (birdSnapshot in snapshot.children) {
                    birdSnapshot.ref.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Database", "Error deleting data", error.toException())
            }
        })
    }



}
