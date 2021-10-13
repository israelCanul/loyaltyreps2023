package com.xcaret.loyaltyreps.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.model.XUserPuntoPos
import com.xcaret.loyaltyreps.util.AppPreferences

class XPuntosAdapter(
    var context: Context? = null,
    private var listOfPoints: ArrayList<XUserPuntoPos>
) : RecyclerView.Adapter<XPuntosAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.cardview_profile_record_item, parent, false))
    }

    override fun getItemCount(): Int = listOfPoints.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mPoint = listOfPoints[position]

        holder.pointsTitle.text = mPoint.comentario
        val pointstext = "+ ${mPoint.puntos} pts"
        holder.pointsTotal.text = pointstext
        holder.pointsDate.text = AppPreferences.formatStringToDate(mPoint.fecha)
        holder.comentarios.visibility = View.GONE

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pointsTitle = itemView.findViewById<TextView>(R.id.recordTitle)
        val pointsTotal = itemView.findViewById<TextView>(R.id.recordPoints)
        val pointsDate = itemView.findViewById<TextView>(R.id.recordDate)
        val comentarios = itemView.findViewById<TextView>(R.id.comentarios)
    }
}