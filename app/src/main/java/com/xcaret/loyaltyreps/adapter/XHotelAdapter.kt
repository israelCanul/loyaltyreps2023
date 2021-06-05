package com.xcaret.loyaltyreps.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.pickup_schedule_table_row.view.*
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.model.PickUpHotel
import java.text.Normalizer
import java.text.SimpleDateFormat

class XHotelAdapter (
    var context: Context? = null,
    private var pickUpHotels: ArrayList<PickUpHotel>
) : RecyclerView.Adapter<XHotelAdapter.ViewHolder>(), Filterable {

    var filteredNewsList: ArrayList<PickUpHotel> = pickUpHotels

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.pickup_schedule_table_row, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pickup = filteredNewsList[position]

        holder.pu_title.text = pickup.dsNombreHotel
        holder.pu_schedule.text = convertTime(pickup.hrPickup).replace(" ", "")
    }

    override fun getItemCount() = filteredNewsList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val pu_title : TextView = itemView.puHotelName
        val pu_schedule : TextView = itemView.schedule
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    filteredNewsList = pickUpHotels
                } else {
                    val filteredList = ArrayList<PickUpHotel>()
                    for (row in pickUpHotels) {
                        if (flattenToAscii(row.dsNombreHotel).toLowerCase().contains(charString.toLowerCase())
                            || flattenToAscii(row.hrPickup).contains(charSequence!!)) {
                            filteredList.add(row)
                        }
                    }
                    filteredNewsList = filteredList
                }
                val filterResults = Filter.FilterResults()
                filterResults.values = filteredNewsList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
                filteredNewsList = filterResults?.values as ArrayList<PickUpHotel>
                notifyDataSetChanged()
            }
        }
    }

    fun flattenToAscii(s: String?): String {
        return if (s == null || s.trim { it <= ' ' }.isEmpty()) "" else Normalizer.normalize(s, Normalizer.Form.NFD).replace("[\u0300-\u036F]".toRegex(), "")
    }

    @SuppressLint("SimpleDateFormat")
    private fun convertTime(inputTime: String) : String {
        val inputFormat = SimpleDateFormat("HH:mm:ss")
        val timeOutputFormat = SimpleDateFormat("hh:mm aa")

        return timeOutputFormat.format(inputFormat.parse(inputTime)!!)
    }

}