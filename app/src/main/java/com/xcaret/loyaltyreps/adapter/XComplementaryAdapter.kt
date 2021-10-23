package com.xcaret.loyaltyreps.adapter

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.cardview_complimentary.view.*
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.model.Complimentary
import com.xcaret.loyaltyreps.model.XComplimentary
import com.xcaret.loyaltyreps.util.AppPreferences

class XComplimentaryAdapter (
    private val context: Context,
    private var activity: Activity? = null,
    private var complimentaries: ArrayList<Complimentary>
) : RecyclerView.Adapter<XComplimentaryAdapter.ViewHolder>(){

    val CALL_PERMISION_CODE = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemContainer = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview_complimentary, parent, false) as ViewGroup

        return ViewHolder(itemContainer)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mcomplimentary = complimentaries[position]

        Glide.with(context).load(mcomplimentary.image).into(holder.comp_cover)
        holder.comp_title.text = mcomplimentary.name

        val mavailability: String = mcomplimentary.noPaxPorUtilizar.toString() + "/" + mcomplimentary.noPaxBeneficio.toString() + " cortesías disponibles"
        holder.comp_availability.text = mavailability


        holder.comp_notice.text = AppPreferences.emptyString(mcomplimentary.note)
        holder.comp_notice.visibility = if (mcomplimentary.note.isEmpty()) View.GONE else View.VISIBLE

        when(mcomplimentary.action){
            "none" -> {
                //holder.comp_notice.visibility = View.GONE
                holder.comp_button.visibility = View.GONE
            }
            "book" -> {
                if (mcomplimentary.noPaxPorUtilizar > 0){
                    holder.comp_button.visibility = View.VISIBLE
                    holder.comp_button.text = context.resources.getString(R.string.make_reservation_text)
                    holder.comp_button.setOnClickListener {
                        val bundle = Bundle().also { it.putParcelable("complimentary", mcomplimentary) }
                        holder.itemView.findNavController().navigate(R.id.to_complimentaryDetailsFragment, bundle)
                    }
                } else {
                    holder.comp_button.visibility = View.GONE
                }
            }
            "call" -> {
                holder.comp_availability.visibility = View.GONE
                holder.comp_button.visibility = View.VISIBLE
                holder.comp_button.text = context.resources.getString(R.string.make_phone_call)
                holder.comp_button.setOnClickListener {
                    if (ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity!!,
                            arrayOf(Manifest.permission.CALL_PHONE),
                            CALL_PERMISION_CODE)
                    }else{
                        try {
                            val callIntent = Intent(Intent.ACTION_CALL)
                            callIntent.data = Uri.parse("tel:${mcomplimentary.phone}")
                            callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(callIntent)
                        }catch (er:Error){
                            println(er)
                        }
                    }
                }

            }
        }
    }

    override fun getItemCount(): Int = complimentaries.size

    class ViewHolder(itemViewGroup: ViewGroup) : RecyclerView.ViewHolder(itemViewGroup){
        val comp_cover: ImageView = itemViewGroup.compCover
        val comp_title: TextView = itemViewGroup.compTitle
        val comp_availability: TextView = itemViewGroup.compAvailability
        val comp_notice: TextView = itemViewGroup.compNotice
        val comp_button: Button = itemViewGroup.compAction
    }

}