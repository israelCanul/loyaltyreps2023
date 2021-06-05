package com.xcaret.loyaltyreps.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.model.XProduct
import com.xcaret.loyaltyreps.util.AppPreferences
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class XProductAdapter(
    val clickListener: (XProduct) -> Unit,
    private val context: Context,
    private val resource: Int,
    private var xProducts: ArrayList<XProduct>
) : RecyclerView.Adapter<XProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): XProductAdapter.ViewHolder {
        val itemContainer = LayoutInflater.from(parent.context)
            .inflate(resource, parent, false) as ViewGroup

        val viewHolder = ViewHolder(itemContainer)

        itemContainer.setOnClickListener {
            clickListener(xProducts[viewHolder.adapterPosition])
        }

        return viewHolder
    }

    override fun getItemCount() = xProducts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val product = xProducts[position]
        if (product.isVisible){
            if (product.cnHotSale){
                holder.hotSaleExpirationContainer.visibility = View.VISIBLE
                holder.hotsateDateExpiration.text = AppPreferences.formatDate(product.feVigencia!!)
            }
            holder.itemView.visibility = View.VISIBLE
            holder.bind(context, product)
            holder.itemView.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        } else {
            holder.container.visibility = View.GONE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: ConstraintLayout = itemView.findViewById(R.id.prodContainer)
        val pcover: ImageView = itemView.findViewById(R.id.product_cover)
        val ptitle: TextView = itemView.findViewById(R.id.product_title)
        val ppoints: TextView = itemView.findViewById(R.id.product_points)
        val hotSaleExpirationContainer: LinearLayout = itemView.findViewById(R.id.expirationDate)
        val hotsateDateExpiration: TextView = itemView.findViewById(R.id.availabilityDate)

        fun bind(context: Context, xProduct: XProduct){
            Glide.with(context).load(xProduct.foto).into(pcover)
            ptitle.text = xProduct.nombre
            val mpuntos = "${NumberFormat.getNumberInstance(Locale.US).format(xProduct.puntos)} pts"
            ppoints.text = mpuntos
        }
    }

}