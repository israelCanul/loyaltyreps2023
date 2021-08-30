package com.xcaret.loyaltyreps.view.fragments.training

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_training_park_details.*
import org.json.JSONObject
import com.xcaret.loyaltyreps.adapter.XTrainingSlideAdapter
import com.xcaret.loyaltyreps.database.XCaretLoyaltyDatabase
import com.xcaret.loyaltyreps.databinding.FragmentTrainingParkDetailsBinding
import com.xcaret.loyaltyreps.model.XImageSlide
import com.xcaret.loyaltyreps.model.XTraining
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory
import java.lang.Exception
import androidx.lifecycle.Observer
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions

class TrainingParkDetailsFragment : Fragment() {

    lateinit var binding: FragmentTrainingParkDetailsBinding

    var END_POINT = "training_section?park_id="
    var xpark_id = ""
    var xvideoUrl = ""
    var userTarjeta = ""

    var mImages: ArrayList<XImageSlide> = ArrayList()
    private var slideSize = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, com.xcaret.loyaltyreps.R.layout.fragment_training_park_details, container, false)

        xpark_id = arguments?.getString("xpark_id").toString()

        loadXParkDetails(xpark_id)
        binding.xparkVideoCover.setOnClickListener { videoCoverClicked() }



        return binding.root
    }

    private fun loadXParkDetails(xparkID: String?){
        mImages.clear()
        AndroidNetworking.get(AppPreferences.PUNK_API_URL+END_POINT+xparkID)
            .setPriority(Priority.LOW)
            .addHeaders("Authorization", "token "+ AppPreferences.PUNK_API_TOKEN)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    try {
                        binding.parktitle.text = arguments?.getString("xpark_name")

                        binding.xparkDescription.text = HtmlCompat.fromHtml(response!!.getString("description"), HtmlCompat.FROM_HTML_MODE_LEGACY)

                        EventsTrackerFunctions.trackTrainingParkEvent(arguments?.getString("xpark_name").toString())

                        Glide.with(this@TrainingParkDetailsFragment)
                            .load(response.getString("cover_img")).into(binding.xparkVideoCover)
                        xvideoUrl = response.getString("video")

                        for (item in 0 until response.getJSONArray("training_details").length()){
                            val singleItem = response.getJSONArray("training_details").getJSONObject(item)
                            val xitem = XTraining(
                                singleItem.getInt("id"),
                                singleItem.getString("name"),
                                singleItem.getString("description"),
                                singleItem.getInt("training_section")
                            )
                            creatTableItem(xitem)
                        }

                        if (response.getJSONArray("images").length() > 0){
                            for (sitem in 0 until response.getJSONArray("images").length()){
                                val mimage = response.getJSONArray("images").getJSONObject(sitem)
                                mImages.add(
                                    XImageSlide(mimage.getString("name"), mimage.getString("image"))
                                )
                            }
                            loadXTPSlide(mImages)
                        }
                    } catch (exception: Exception){
                        exception.printStackTrace()
                    }
                }

                override fun onError(anError: ANError?) {
                    println("eeeo$anError")
                }

            })
    }



    private fun videoCoverClicked(){
        val bundle = Bundle().also {
            it.putString("xvideo_url", xvideoUrl)
            it.putString("video_id", "1")
        }
        EventsTrackerFunctions.trackTrainingParkSectionEvent(binding.parktitle.text.toString(),"video")
        findNavController().navigate(com.xcaret.loyaltyreps.R.id.to_XVideoActivity, bundle)
    }

    private fun creatTableItem(xTraining: XTraining){
        val inflater2 = this.layoutInflater
        val item_row2 = inflater2.inflate(com.xcaret.loyaltyreps.R.layout.tablerow_training_xpark_training_details, null)

        item_row2.findViewById<TextView>(com.xcaret.loyaltyreps.R.id.gotoDetails).text = xTraining.name

        item_row2.setOnClickListener {
            val mbundle = Bundle()
            EventsTrackerFunctions.trackTrainingParkSectionEvent(binding.parktitle.text.toString(),xTraining.name.toString())
            mbundle.putParcelable("xtraining_item", xTraining)
            findNavController().navigate(com.xcaret.loyaltyreps.R.id.to_trainingParkTDetailsFragment, mbundle)
        }

        detailsTablelayout.addView(item_row2)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun loadXTPSlide(images: ArrayList<XImageSlide>){
        binding.xParkSlideViewPager.adapter = XTrainingSlideAdapter(context!!,
            images, com.xcaret.loyaltyreps.R.layout.cardview_slite_item, 1,binding.parktitle.text.toString())

        slideSize = images.size

        binding.xParkSlideViewPager.addOnPageChangeListener(object  : ViewPager.OnPageChangeListener {
            var thresholdOffset: Float = 0.5f
            var thresholdOffsetPixels: Int = 1
            var scrollStarted: Boolean = false
            var checkDirection: Boolean = false

            override fun onPageScrollStateChanged(state: Int) {

                if (!scrollStarted && state == ViewPager.SCROLL_STATE_DRAGGING) {
                    scrollStarted = true
                    checkDirection = true

                } else {
                    scrollStarted = false
                }
            }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                if (checkDirection) {
                    if (thresholdOffset > positionOffset && positionOffsetPixels > thresholdOffsetPixels) {

                    } else{
                        if (position == slideSize.minus(1) && scrollStarted) {
                            binding.xParkSlideViewPager.setCurrentItem(0, false)
                        }
                    }
                }
                checkDirection = false
            }

            override fun onPageSelected(position: Int) {
            }

        })

        binding.slideIndicator.setupWithViewPager(binding.xParkSlideViewPager)
    }

}
