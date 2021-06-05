package com.xcaret.loyaltyreps.adapter

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.model.XPark
import kotlinx.android.synthetic.main.cardview_training_tour.view.*

class XParkAdapter (
    private var viewType: Int,
    private val context: Context,
    private val resource: Int,
    private var xparksList: ArrayList<XPark>
) : RecyclerView.Adapter<XParkAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context)
            .inflate(resource, parent, false) as ViewGroup)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val xpark = xparksList[position]

        holder.park_title.text = xpark.name
        holder.park_title.setTextColor(Color.parseColor(xpark.color))
        Glide.with(context).load(xpark.logo).into(holder.park_cover)

        val bundle = Bundle().also {
            it.putString("xpark_name", xpark.name)
            it.putString("xpark_id", xpark.id.toString())
        }

        holder.park_title.setOnClickListener {
            if (viewType == 1) {
                holder.itemView.findNavController().navigate(R.id.to_parkDetailsFragment, bundle)
            } else if (viewType == 2){
                holder.itemView.findNavController().navigate(R.id.to_trainingParkDetailsFragment, bundle)
            }
        }
        holder.park_cover.setOnClickListener {
            if (viewType == 1) {
                holder.itemView.findNavController().navigate(R.id.to_parkDetailsFragment, bundle)
            } else if (viewType == 2){
                holder.itemView.findNavController().navigate(R.id.to_trainingParkDetailsFragment, bundle)
            }
        }
    }

    override fun getItemCount() = xparksList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val park_title : TextView = itemView.parkTitle
        val park_cover : ImageView = itemView.parkCover
    }
}