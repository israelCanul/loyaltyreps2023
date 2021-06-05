package com.xcaret.loyaltyreps.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.cardview_quiz_choice_item.view.*
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.model.XQuestionChoice

class XChoiceAdapter (
    private val context: Context,
    private var choices: ArrayList<XQuestionChoice>
) : RecyclerView.Adapter<XChoiceAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemContainer = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview_quiz_choice_item, parent, false) as ViewGroup

        val viewHolder = ViewHolder(itemContainer)

        return viewHolder
    }

    override fun getItemCount(): Int = choices.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mychoice = choices[position]

        holder.mchoice.id = mychoice.id
        holder.mchoice.isChecked = true
        holder.mchoice.textOff = mychoice.option
        holder.mchoice.textOn = mychoice.option
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mchoice = itemView.quizChoice
    }

}