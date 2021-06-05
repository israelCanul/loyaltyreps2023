package com.xcaret.loyaltyreps.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.cardview_support.view.*
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.model.XSQuestion
import com.xcaret.loyaltyreps.model.XSupportSubject

class XSupportAdapter (
    var context: Context,
    //val clickListener: (XSupportSubject) -> Unit,
    var subjects: ArrayList<XSupportSubject> = ArrayList()
) : RecyclerView.Adapter<XSupportAdapter.ViewHolder>() {

    var myfaqs: ArrayList<XSQuestion> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.cardview_support, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msubject = subjects[position]

        myfaqs = subjects[position].faqs
        holder.bind(context, msubject, myfaqs)

        holder.itemView.setOnClickListener {
            val expanded = msubject.expanded
            msubject.expanded = !expanded

            if (!msubject.expanded){
                holder.sub_item.removeAllViews()
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = subjects.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val sub_item: LinearLayout = itemView.sub_item
        val chevron_down: ImageButton = itemView.chevronDown
        val chevron_up: ImageButton = itemView.chevronUp
        val mquestion: TextView = itemView.question

        fun bind(context: Context, msubject: XSupportSubject, myfaqs: ArrayList<XSQuestion>){
            val expanded = msubject.expanded

            if (expanded) {
                sub_item.visibility = View.VISIBLE
                //myfaqs = msubject.faqs
                for (item in myfaqs){
                    if (item.idDad == msubject.id){
                        createTableRowItem(context, msubject.question, item.question!!, item.answer!!)
                    }
                }
            } else {
                sub_item.visibility = View.GONE
            }
            //sub_item.visibility = if (expanded) View.VISIBLE else View.GONE

            chevron_down.visibility = if (expanded) View.GONE else View.VISIBLE
            chevron_up.visibility = if (expanded) View.VISIBLE else View.GONE

            mquestion.text = msubject.question

        }

        fun createTableRowItem(mcontext: Context, msubject: String, mquestion: String, manswer: String) {
            val minflater = LayoutInflater.from(mcontext).inflate(R.layout.faqs_item, null, false)

            minflater.findViewById<TextView>(R.id.faqTitle).text = mquestion

            minflater.findViewById<TextView>(R.id.faqTitle).setOnClickListener {
                val bundle = Bundle().also {
                    it.putString("msubject", msubject)
                    it.putString("mquestion", mquestion)
                    it.putString("manswer", manswer)
                }
                itemView.findNavController().navigate(R.id.to_supportQuestionDetailsFragment, bundle)
            }

            sub_item.addView(minflater)
        }

    }


}