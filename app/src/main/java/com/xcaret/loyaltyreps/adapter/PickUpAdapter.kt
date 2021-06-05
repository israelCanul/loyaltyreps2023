package com.xcaret.loyaltyreps.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.pickup_schedule_table_row.view.*
import com.xcaret.loyaltyreps.model.PickUpHotel
import java.text.SimpleDateFormat

class PickUpAdapter (
    var context: Context? = null,
    var resource: Int? = null,
    private var pickUpHotels: ArrayList<PickUpHotel>
) : RecyclerView.Adapter<PickUpAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(resource!!, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pickup = pickUpHotels[position]

        holder.pu_title.text = pickup.dsNombreHotel
        holder.pu_schedule.text = convertTime(pickup.hrPickup).replace(" ", "")
    }

    override fun getItemCount() = pickUpHotels.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val pu_title : TextView = itemView.puHotelName
        val pu_schedule : TextView = itemView.schedule
    }

    @SuppressLint("SimpleDateFormat")
    fun convertTime(inputTime: String) : String {
        val inputFormat = SimpleDateFormat("HH:mm:ss")
        val timeOutputFormat = SimpleDateFormat("hh:mm aa")

        return timeOutputFormat.format(inputFormat.parse(inputTime)!!)
    }
}