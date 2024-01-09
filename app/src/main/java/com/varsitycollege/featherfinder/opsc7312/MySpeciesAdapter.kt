package com.varsitycollege.featherfinder.opsc7312

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

//adapter is used to feed info to recycler view
//class takes in an array list of objects from data class as params. Class extends recycler view . Implement members (override)
class MySpeciesAdapter(private val birdList: ArrayList<Data.BirdSpecies>) : RecyclerView.Adapter<MySpeciesAdapter.MyBirdViewHolder>() {

    //for item click on recycler view
    private lateinit var myListener: I_onBirdClickListener

    interface I_onBirdClickListener {

        fun onBirdClick(position: Int)
    }

    fun setOnBirdClick(listener: I_onBirdClickListener) {
        myListener = listener
    }

    //Favourite methods remove and add
    fun removeBirdGuide(i: Int) {
        birdList.removeAt(i)
        notifyDataSetChanged()
    }

    fun showFavouriteGuide(i: Int, bird: Data.BirdSpecies) {
        birdList.add(i, bird)//place bird at the end of the list (for now)
        notifyDataSetChanged()
    }

    //implemented members
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBirdViewHolder {
        // assign view and pass as param in constructor. inflate the recycler layout that you made
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_guideitem, parent, false)
        return MyBirdViewHolder(itemView, myListener)// returning a MyBirdViewHolder object.
    }

    override fun getItemCount(): Int {
        return birdList.size //tells adapter how many items are in recycler view
    }

    //method to retrieve data from arraylist. Take in the MyBirdViewHolder object and a position.
    override fun onBindViewHolder(holder: MyBirdViewHolder, position: Int) {
        val currentGuideItem = birdList[position]
        holder.speciesCommonName.text = currentGuideItem.commonName
        holder.speciesScientificName.text = currentGuideItem.scientificName

        // Set the tag to the current bird species
        holder.itemView.tag = currentGuideItem
    }

    fun setData(newData: ArrayList<Data.BirdSpecies>) {
        val newList = ArrayList(newData)
        birdList.clear()
        birdList.addAll(newList)
        notifyDataSetChanged()
    }

    //used to create object with variables relating to the xml elements in the recycler view
    //extend recycler view again. Take in a view as a param . This class deals with the image and text views
    //init is seen as a constructor
    class MyBirdViewHolder(itemView: View, listener: I_onBirdClickListener) : RecyclerView.ViewHolder(itemView) {

        //elements in recycler view
        //val speciesImage : ShapeableImageView = itemView.findViewById(R.id.birdImage)
        val speciesCommonName: TextView = itemView.findViewById(R.id.birdCommonName)
        val speciesScientificName: TextView = itemView.findViewById(R.id.birdScientificName)

        init {
            itemView.setOnClickListener {
                listener.onBirdClick(adapterPosition)
            }
        }
    }
}
