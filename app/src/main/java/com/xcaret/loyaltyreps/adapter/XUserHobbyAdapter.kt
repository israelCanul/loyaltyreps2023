package com.xcaret.loyaltyreps.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.model.XUserHobby
import com.xcaret.loyaltyreps.util.AppPreferences

class XUserHobbyAdapter(
    private val context: Context,
    private var listOfHobbies: ArrayList<XUserHobby>,
    private var listOfIds: List<Int>
) : RecyclerView.Adapter<XUserHobbyAdapter.ViewHolder>() {

    private lateinit var selected: Drawable
    private lateinit var unselected: Drawable

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemContainer = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview_hobby_item, parent, false) as ViewGroup

        val viewHolder = ViewHolder(itemContainer)

        return viewHolder
    }

    override fun getItemCount(): Int = listOfHobbies.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mHobby = listOfHobbies[position]

        holder.m_toggle.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        holder.m_toggle.compoundDrawablePadding = 15
        holder.m_toggle.id = mHobby.id
        holder.m_toggle.textOn = mHobby.dsDescripcion
        holder.m_toggle.textOff = mHobby.dsDescripcion
        holder.m_toggle.isChecked = true

        selected = ContextCompat.getDrawable(context, R.drawable.toggle_on)!!
        unselected = ContextCompat.getDrawable(context, R.drawable.toggle_off)!!

        for (mid in listOfIds){
            if (mid == mHobby.id) {
                AppPreferences.selectedInterestsIds.add(mHobby.id)
                holder.m_toggle.setCompoundDrawablesWithIntrinsicBounds(selected, null, null, null)
            }
        }
        holder.m_toggle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                buttonView.setCompoundDrawablesWithIntrinsicBounds(selected, null, null, null)
                AppPreferences.selectedInterestsIds.add(buttonView.id)
            } else {
                buttonView.setCompoundDrawablesWithIntrinsicBounds(unselected, null, null, null)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var m_toggle = itemView.findViewById<ToggleButton>(R.id.userHobby)
    }
}