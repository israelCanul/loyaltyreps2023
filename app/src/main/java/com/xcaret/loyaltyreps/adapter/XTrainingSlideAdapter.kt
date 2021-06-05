package com.xcaret.loyaltyreps.adapter

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.findNavController
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.model.XImageSlide

class XTrainingSlideAdapter(
    var context: Context,
    private var images: ArrayList<XImageSlide>,
    var resource: Int ?= null,
    var origin: Int? = null
    ): PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as View
    }

    override fun getCount(): Int {
        return images.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any{
        val image = images[position] //[position]
        val view = LayoutInflater.from(container.context).inflate(resource!!, container,
            false)

        if (origin == 1) {
            val imageViewItem: ImageView = view.findViewById(R.id.slideImage)

            Glide.with(context)
                .load(image.image)
                .into(imageViewItem)
            imageViewItem.setOnClickListener {
                val bundle = Bundle()
                bundle.putParcelableArrayList("images", images)
                bundle.putInt("position", position)
                it.findNavController().navigate(R.id.action_trainingParkDetailsFragment_to_fullSlideActivity, bundle)
            }
        } else {
            val imageViewItem: PhotoView = view.findViewById(R.id.slideFullImage)
            Glide.with(context)
                .load(image.image)
                .into(imageViewItem)
        }

        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}