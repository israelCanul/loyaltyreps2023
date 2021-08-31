package com.xcaret.loyaltyreps.view.fragments.xparks


import android.Manifest
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.koushikdutta.ion.Ion
import org.json.JSONObject

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.FragmentParkDetailsBinding
import com.xcaret.loyaltyreps.model.XParkInfographic
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions.trackParkSectionEvent
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class ParkDetailsFragment : Fragment() {

    lateinit var binding: FragmentParkDetailsBinding
    var END_POINT = "infographics/"
    var xpark_id = ""
    var infographic_url = ""
    var xparkName:String = ""

    var xparksinfogs: ArrayList<XParkInfographic> = ArrayList()
    var langSpinnerAdapter: ArrayAdapter<XParkInfographic>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_park_details, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        xpark_id = arguments?.getString("xpark_id").toString()

        xparkName = arguments?.getString("xpark_name").toString()
        loadParksInfographics()

        populateParksInfographicSpinner()

        binding.shareThisInfographic.setOnClickListener { shareInfographic() }
    }

    private fun loadParksInfographics(){
        AndroidNetworking.get(AppPreferences.PUNK_API_URL+"$END_POINT$xpark_id")
            .setPriority(Priority.MEDIUM)
            .addHeaders("Authorization", "token "+ AppPreferences.PUNK_API_TOKEN)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    binding.progressBar.visibility = View.GONE
                    try {
                        val listOfZones = response.getJSONArray("infographics")
                        for (item in 0 until listOfZones.length()){
                            val minfog = listOfZones.getJSONObject(item)
                            xparksinfogs.add(
                                XParkInfographic(
                                    minfog.getInt("id"),
                                    minfog.getString("language"),
                                    minfog.getString("image"),
                                    minfog.getInt("park")
                                )
                            )

                        }

                    } catch (excep: Exception) {
                        excep.printStackTrace()
                    }
                    notifyAdapter()
                }
                override fun onError(error: ANError) {
                    binding.progressBar.visibility = View.GONE
                    AppPreferences.toastMessage(activity!!, "¡Algo salió mal, inténtalo nuevamente!")
                }
            })
    }

    private fun populateParksInfographicSpinner(){
        langSpinnerAdapter = ArrayAdapter(activity!!, R.layout.spinner_infographic_item, xparksinfogs)

        binding.selectLanguage.adapter = langSpinnerAdapter
        binding.selectLanguage.onItemSelectedListener = langSpinnerListener


    }

    private fun notifyAdapter() {
        try {
            activity!!.runOnUiThread(Runnable {
                if (langSpinnerAdapter != null) {
                    langSpinnerAdapter!!.notifyDataSetChanged()
                }
            })
        } catch (exep: Exception){
            exep.printStackTrace()
        }
    }

    private val langSpinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val mLang = parent.selectedItem as XParkInfographic
            Glide.with(activity!!).load(mLang.image).into(binding.parkLangInfographic)
            infographic_url = mLang.image!!
            (parent.getChildAt(0) as TextView).setTextColor(Color.parseColor("#ffffff"))
            //println("firebase se" + xparkName.toLowerCase().capitalize() + "_" + mLang.language)
            trackParkSectionEvent(xparkName.toLowerCase().capitalize() + "_" + mLang.language)
        }
        override fun onNothingSelected(parent: AdapterView<*>) {
        }
    }


    private fun shareInfographic(){
        val bitmap = Ion.with(activity!!)
            .load(infographic_url)
            .asBitmap()
            .get()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionCheck: Int = ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )

            val permissionCheck2: Int = ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            when {
                permissionCheck != PackageManager.PERMISSION_GRANTED -> ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
                permissionCheck2 != PackageManager.PERMISSION_GRANTED -> ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)

            }
        }

        val wrapper = ContextWrapper(activity)
        var file = wrapper.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        file = File(file, "xcaret-loyalty-${UUID.randomUUID()}.jpg")

        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)
            // Compress bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            // Flush the stream
            stream.flush()

            // Close stream
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var finalUri: Uri? = null
        finalUri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Uri.fromFile(file)
        } else {
            FileProvider.getUriForFile(activity!!, context!!.packageName, file)
        }

        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, finalUri)
            type = "image/*"
        }
        val langShare = binding.selectLanguage.selectedItem as XParkInfographic
        //println("firebase se" + xparkName.toLowerCase().capitalize() + "_" + langShare.language)
        trackParkSectionEvent(xparkName.toLowerCase().capitalize() + "_" + langShare.language + "_Share")
        startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.share_using)))
    }

}
