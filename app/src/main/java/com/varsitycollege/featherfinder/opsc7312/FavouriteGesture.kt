import android.content.Context
import android.graphics.Canvas
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.varsitycollege.featherfinder.opsc7312.Data
import com.varsitycollege.featherfinder.opsc7312.MySpeciesAdapter
import com.varsitycollege.featherfinder.opsc7312.R
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

abstract class FavouriteGesture(
    private val context: Context,
    private val databaseReference: DatabaseReference
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val myFavCol = ContextCompat.getColor(context, R.color.favouritedColor)
    private val favIcon = R.drawable.favourited_icon

    // Firebase Authentication
    private val auth = FirebaseAuth.getInstance()

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (!isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, 0f, dY, actionState, false)
        } else {
            RecyclerViewSwipeDecorator.Builder(
                c,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
                .addSwipeLeftBackgroundColor(myFavCol)
                .addSwipeLeftActionIcon(favIcon)
                .create()
                .decorate()

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }



    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        when (direction) {
            ItemTouchHelper.LEFT -> {
                val favItem = (viewHolder as MySpeciesAdapter.MyBirdViewHolder).itemView.tag as Data.BirdSpecies

                // Get the current user ID
                val currentUser = auth.currentUser
                val userId = currentUser?.uid

                if (userId != null) {
                    // Store swiped bird information in the real-time database along with user id
                    databaseReference.child(userId).push().setValue(favItem)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("Database", "Data added successfully")
                                Toast.makeText(
                                    context,
                                    "Guide added to favourites",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Log.e("Database", "Error adding data to the database", task.exception)
                                Toast.makeText(
                                    context,
                                    "Failed to add guide to favourites",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    // Handle the case when the user is not authenticated
                    Log.e("Database", "User not authenticated")
                    Toast.makeText(
                        context,
                        "Authentication error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}
