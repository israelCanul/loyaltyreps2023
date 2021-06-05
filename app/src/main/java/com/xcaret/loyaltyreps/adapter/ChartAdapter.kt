package com.xcaret.loyaltyreps.adapter

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.chart_item.view.*
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.model.ChartItem
import kotlin.math.roundToInt


class ChartAdapter (
    private val context: Context,
    private var chartItems: ArrayList<ChartItem>
) : RecyclerView.Adapter<ChartAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemContainer = LayoutInflater.from(parent.context)
            .inflate(R.layout.chart_item, parent, false) as ViewGroup

        return ViewHolder(itemContainer)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chart_item = chartItems[position]

        Glide.with(context).load(getImage(chart_item.logo)).into(holder.park_logo)

        Handler().postDelayed({
            holder.park_status.layoutParams.height = (((chart_item.noPax).toFloat() / totalPax().toFloat())*2000).toInt()
            holder.park_status.background = ContextCompat.getDrawable(context, getImage(chart_item.background))
        }, 500)
    }

    override fun getItemCount(): Int = chartItems.size

    class ViewHolder(itemViewGroup: ViewGroup) : RecyclerView.ViewHolder(itemViewGroup){
        val park_logo: ImageView = itemViewGroup.parkLogo
        val park_status: View = itemViewGroup.parkStatus
    }

    fun getImage(imageName: String): Int {
        return context.resources.getIdentifier(imageName, "drawable", context.packageName)
    }

    private fun slideUpView(mV: View, mHeight: Float){
        val animate = TranslateAnimation(
            0f, // fromXDelta
            0f, // toXDelta
            mHeight*2, // fromYDelta
            0f
        )                // toYDelta
        animate.duration = 500
        animate.fillAfter = true
        mV.startAnimation(animate)
    }

    fun totalPax() : Int {
        var totPax = 0
        for (item in chartItems) {
            totPax += item.noPax
        }
        return totPax
    }
}