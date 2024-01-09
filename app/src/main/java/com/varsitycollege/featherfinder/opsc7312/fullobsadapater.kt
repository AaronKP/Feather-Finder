package com.varsitycollege.featherfinder.opsc7312

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class fullobsadapter(private val observationsList: ArrayList<Data.Observation>):
    RecyclerView.Adapter<fullobsadapter.MyViewHolder> (){
    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val tvComName : TextView = itemView.findViewById(R.id.myComNametxt)
        val tvSciName : TextView = itemView.findViewById(R.id.mySciNametxt)
        val tvTheDate : TextView = itemView.findViewById(R.id.myDatetxt)
        val tvTheTime : TextView = itemView.findViewById(R.id.myTimetxt)
        val tvTheLocation: TextView = itemView.findViewById(R.id.myLocationtxt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.full_observations_view, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return observationsList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvComName.text = observationsList[position].comName
        holder.tvSciName.text = observationsList[position].sciName
        holder.tvTheDate.text = formatDate(observationsList[position].date)
        holder.tvTheTime.text = observationsList[position].time
        holder.tvTheLocation.text = observationsList[position].location

        // Load and display the image using Glide
        val imageView = holder.itemView.findViewById<CircleImageView>(R.id.birdImg1)
        val imageUrl = observationsList[position].imageUrl

        // Use Glide to load the image from Firebase Storage
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.birddefault) // You can use a placeholder image
            .error(R.drawable.birddefault) // You can use an error image
            .into(imageView)

        // Set click listener to navigate to the detail activity
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val observation = observationsList[position]

            val intent = Intent(context, ObservationDetail::class.java)
            intent.putExtra("observationData", observation)  // Pass the serialized observation instance
            context.startActivity(intent)
        }
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

}