package com.xcaret.loyaltyreps.view.fragments.training

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.BuildConfig
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.xcaret.loyaltyreps.adapter.XTrainingSlideAdapter
import com.xcaret.loyaltyreps.databinding.FragmentTrainingParkDetailsBinding
import com.xcaret.loyaltyreps.model.XImageSlide
import com.xcaret.loyaltyreps.model.XTraining
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions
import kotlinx.android.synthetic.main.fragment_training_park_details.*
import org.json.JSONObject

class TrainingParkDetailsFragment : Fragment() {

    lateinit var binding: FragmentTrainingParkDetailsBinding

    var END_POINT = "training_section?park_id="
    var xpark_id = ""
    var xvideoUrl = ""
    var userTarjeta = ""

    var mImages: ArrayList<XImageSlide> = ArrayList()
    private var slideSize = 0
    var mydownloadID : Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, com.xcaret.loyaltyreps.R.layout.fragment_training_park_details, container, false)

        xpark_id = arguments?.getString("xpark_id").toString()

        loadXParkDetails(xpark_id)
        binding.xparkVideoCover.setOnClickListener { videoCoverClicked() }

    var br = object:BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            var id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1)
            if(id==mydownloadID){
                Toast.makeText(context, "Download Completed", Toast.LENGTH_LONG).show()
            }
        }

    }
    context?.registerReceiver(br,IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        return binding.root
    }

    fun permitDiskReads(func: () -> Any?) : Any? {
        if (BuildConfig.DEBUG) {
            val oldThreadPolicy = StrictMode.getThreadPolicy()
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder(oldThreadPolicy)
                    .permitDiskReads().build())
            val anyValue = func()
            StrictMode.setThreadPolicy(oldThreadPolicy)

            return anyValue
        } else {
            return func()
        }
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

                        binding.downloadvideo.setOnClickListener {


                            kotlin.run {
                                var request = DownloadManager.Request(Uri.parse(xvideoUrl))
                                    .setTitle(arguments?.getString("xpark_name").toString())
                                    .setDescription(arguments?.getString("xpark_name").toString() + "Downloading")
                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                                    .setAllowedOverMetered(true)
                                    //.setDestinationInExternalPublicDir(context?.getExternalFilesDir(Environment.DIRECTORY_DCIM).toString() ,arguments?.getString("xpark_name").toString() + ".mp4")
                                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,arguments?.getString("xpark_name").toString() + ".mp4")
                                var dm:DownloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                mydownloadID =  dm.enqueue(request)
                                Toast.makeText(context, "Downloading ${arguments?.getString("xpark_name").toString()}", Toast.LENGTH_LONG).show()
                            }


                        }



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
