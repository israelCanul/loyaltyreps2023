package com.xcaret.loyaltyreps.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.apache.commons.lang3.StringUtils
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.model.XNews
import com.xcaret.loyaltyreps.util.AppPreferences.formatStringToDate
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions
import java.text.Normalizer

class XNewsFeedAdapter(
    var context: Context,
    private var xNewsList: ArrayList<XNews>
) : RecyclerView.Adapter<XNewsFeedAdapter.ViewHolder>(), Filterable{

    var filteredNewsList: ArrayList<XNews> = xNewsList

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        /*val vista = if (viewType == 0) {
            LayoutInflater.from(parent.context).inflate(R.layout.cardview_newsfeed_vertical, parent, false) as ViewGroup
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.cardview_newsfeed_horizontal, parent, false) as ViewGroup
        }*/
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.cardview_newsfeed_horizontal, parent, false) as ViewGroup
        return ViewHolder(vista)
    }

    override fun getItemCount(): Int = filteredNewsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val single_new = filteredNewsList[position]

        Glide.with(context).load(single_new.cover_img).into(holder.news_cover)
        holder.news_title.text = single_new.title
        holder.news_pub_date.text = StringUtils.capitalize(formatStringToDate(single_new.created_at!!))

        holder.itemView.setOnClickListener {
            EventsTrackerFunctions.trackEvent(EventsTrackerFunctions.newsFeedFeaturedView)
            val bundle = Bundle().also { it.putParcelable("xnews", single_new) }
            holder.itemView.findNavController().navigate(R.id.to_newsDetailsFragment, bundle)
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val news_cover: ImageView = itemView.findViewById(R.id.newsCover)
        val news_title: TextView = itemView.findViewById(R.id.newsTitle)
        val news_pub_date: TextView = itemView.findViewById(R.id.newsPublishDate)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    filteredNewsList = xNewsList
                } else {
                    val filteredList = ArrayList<XNews>()
                    for (row in xNewsList) {
                        if (flattenToAscii(row.title).toLowerCase().contains(charString.toLowerCase())
                            || flattenToAscii(row.description).contains(charSequence!!)) {
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
                filteredNewsList = filterResults?.values as ArrayList<XNews>
                notifyDataSetChanged()
            }
        }
    }

    fun flattenToAscii(s: String?): String {
        return if (s == null || s.trim { it <= ' ' }.isEmpty()) "" else Normalizer.normalize(s, Normalizer.Form.NFD).replace("[\u0300-\u036F]".toRegex(), "")
    }

}