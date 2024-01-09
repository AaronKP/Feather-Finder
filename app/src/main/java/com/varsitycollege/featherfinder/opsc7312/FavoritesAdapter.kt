package com.varsitycollege.featherfinder.opsc7312

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoritesAdapter(
    private val favoriteList: ArrayList<Data.BirdSpecies>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_guideitem, parent, false)
        return FavoriteViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return favoriteList.size
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val currentFavorite = favoriteList[position]

        // Set the favorite bird information in the view holder
        holder.favoriteCommonName.text = currentFavorite.commonName
        holder.favoriteScientificName.text = currentFavorite.scientificName

        // Handle item click
        holder.itemView.setOnClickListener {
            listener.onItemClick(currentFavorite)
        }

        // Handle item long click
        holder.itemView.setOnLongClickListener {
            listener.onItemLongClick(currentFavorite)
            true // Consume the long click
        }
    }

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val favoriteCommonName: TextView = itemView.findViewById(R.id.birdCommonName)
        val favoriteScientificName: TextView = itemView.findViewById(R.id.birdScientificName)
    }

    // Item click interface
    interface OnItemClickListener {
        fun onItemClick(birdSpecies: Data.BirdSpecies)
        fun onItemLongClick(birdSpecies: Data.BirdSpecies)
    }

    fun setData(newData: ArrayList<Data.BirdSpecies>) {
        favoriteList.clear()
        favoriteList.addAll(newData)
        notifyDataSetChanged()
        Log.d("Adapter", "Data set updated. New size: ${favoriteList.size}")
    }
}
