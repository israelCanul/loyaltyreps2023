package com.xcaret.loyaltyreps.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.model.XUserPuntosNeg
import com.xcaret.loyaltyreps.util.AppPreferences

class XPuntosNegAdapter(
    var context: Context? = null,
    private var listOfNegPoints: ArrayList<XUserPuntosNeg>
) : RecyclerView.Adapter<XPuntosNegAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.cardview_profile_record_item, parent, false))
    }

    override fun getItemCount(): Int = listOfNegPoints.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val negPoint = listOfNegPoints[position]
        holder.pointsTitle.text = negPoint.articulo
        val pointstext = "- ${negPoint.puntos} pts"
        holder.pointsTotal.text = pointstext
        holder.pointsDate.text = AppPreferences.formatStringToDate(negPoint.fecha)

        if(negPoint.observaciones.isNotBlank())
            holder.comentarios.text = negPoint.observaciones
        else
            holder.comentarios.text = "Sin observaciónes"
        holder.comentarios.visibility = View.VISIBLE
        holder.estatusRecordValue.text = negPoint.estatus
        holder.estatusRecord.visibility = View.VISIBLE
        holder.estatusRecordValue.visibility = View.VISIBLE
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val pointsTitle = itemView.findViewById<TextView>(R.id.recordTitle)
        val pointsTotal = itemView.findViewById<TextView>(R.id.recordPoints)
        val pointsDate = itemView.findViewById<TextView>(R.id.recordDate)
        val estatusRecord = itemView.findViewById<TextView>(R.id.estatusRecord)
        val estatusRecordValue = itemView.findViewById<TextView>(R.id.estatusRecordValue)
        val comentarios = itemView.findViewById<TextView>(R.id.comentarios)
    }
}