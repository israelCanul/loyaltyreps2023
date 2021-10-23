package com.xcaret.loyaltyreps.view.fragments.training

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.bumptech.glide.Glide
import org.json.JSONArray
import com.xcaret.loyaltyreps.MainActivity

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.adapter.XParkAdapter
import com.xcaret.loyaltyreps.adapter.XVideoAdapter
import com.xcaret.loyaltyreps.databinding.FragmentTrainingBinding
import com.xcaret.loyaltyreps.model.XPark
import com.xcaret.loyaltyreps.model.XVideo
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions
import java.lang.Exception

class TrainingFragment : Fragment() {

    lateinit var binding: FragmentTrainingBinding

    private val ENDPOINT_PARKS = "parks/"
    private val ENDPOINT_VIDEOS = "videos/"
    //private val clickListener: (XPark) -> Unit = this::onXParkClicked

    private var recyclerViewAdapter: XParkAdapter? = null
    private var vrecyclerViewAdapter: XVideoAdapter? = null

    private var xVideoList: ArrayList<XVideo> = ArrayList()
    private var xParksList: ArrayList<XPark> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_training, container, false)

        setupRecyclerView()

        loadViews()
        loadXParks()

        val result: List<Int>
        if (!AppPreferences.quizzesIds.isEmpty()) {
            result = AppPreferences.quizzesIds.split(",").map(String::toInt)
        } else {
            result = ArrayList()
            result.add(0)
        }

        loadXVideos(result)

        openPDFile()

        return binding.root
    }

    private fun loadViews(){
        Glide.with(activity!!).load(R.drawable.lore_guia_operativa).centerInside().into(binding.loreGuia)
    }

    private fun loadXParks(){
        xParksList.clear()
        AndroidNetworking.get(AppPreferences.PUNK_API_URL+ENDPOINT_PARKS)
            .setPriority(Priority.LOW)
            .addHeaders("Authorization", "token "+ AppPreferences.PUNK_API_TOKEN)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onError(anError: ANError?) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(activity, "Algo salió mal, inténtalo nuevamente!", Toast.LENGTH_SHORT).show()
                }
                override fun onResponse(response: JSONArray) {
                    binding.progressBar.visibility = View.GONE
                    try {
                        for (item in 0 until response.length()){
                            val xpark_item = response.getJSONObject(item)
                            xParksList.add(
                                XPark(
                                    xpark_item.getInt("id"),
                                    xpark_item.getString("name"),
                                    xpark_item.getString("logo"),
                                    xpark_item.getString("color"),
                                    ArrayList()
                                )
                            )
                        }
                        recyclerViewAdapter = XParkAdapter(2,activity!!, R.layout.cardview_training_tour, xParksList)
                        binding.toursRecyclerView.adapter = recyclerViewAdapter
                        binding.toursRecyclerView.adapter!!.notifyDataSetChanged()
                    } catch (exception: Exception){
                        exception.printStackTrace()
                    }
                }
            })
    }

    private fun onXParkClicked(xPark: XPark){
        val bundle = Bundle().also { it.putString("xpark_id", xPark.id.toString()) }
        view?.findNavController()?.navigate(R.id.to_trainingParkDetailsFragment, bundle)
    }

    private fun loadXVideos(listOfQuizIds: List<Int>){
        xVideoList.clear()
        //https://xcaret.punklabs.ninja
        AndroidNetworking.get(AppPreferences.PUNK_API_URL+ENDPOINT_VIDEOS)
            .setPriority(Priority.LOW)
            .addHeaders("Authorization", "token ${AppPreferences.PUNK_API_TOKEN}")
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onError(anError: ANError?) {
                    Toast.makeText(activity, "Algo salió mal, inténtalo nuevamente!", Toast.LENGTH_SHORT).show()
                }
                override fun onResponse(response: JSONArray) {
                    try {
                        for (item in 0 until response.length()){
                            val xvideo_item = response.getJSONObject(item)

                            xVideoList.add(
                                XVideo(
                                    xvideo_item.getInt("id"),
                                    xvideo_item.getString("name"),
                                    xvideo_item.getString("video"),
                                    xvideo_item.getString("cover_img"),
                                    if (xvideo_item.get("points").toString() == ""
                                        || xvideo_item.get("points").toString() == "null") 0 else xvideo_item.getInt("points"),
                                    xvideo_item.getBoolean("active"),
                                    if (xvideo_item.get("wallet").toString() == "" ||
                                        xvideo_item.get("wallet").toString() == "null") 0 else xvideo_item.getInt("wallet"),
                                    xvideo_item.getInt("quiz_id"),
                                    xvideo_item.getString("deadline"),
                                    xvideo_item.getBoolean("quiz_available"),
                                    true
                                )
                            )
                        }
                        vrecyclerViewAdapter = XVideoAdapter(activity!!, R.layout.cardview_training_video, xVideoList, listOfQuizIds, activity!!)
                        binding.videosRecyclerView.adapter = vrecyclerViewAdapter
                        binding.videosRecyclerView.adapter!!.notifyDataSetChanged()
                    } catch (except: Exception) {
                        except.printStackTrace()
                    }
                }
            })
    }

    private fun openPDFile(){
        binding.loreGuia.setOnClickListener {
            EventsTrackerFunctions.trackEvent(EventsTrackerFunctions.operativeGuideOpen)
            val mainActivity = activity as MainActivity?
            mainActivity!!.openActivityPDFile("Loyalty Reps - Guía", AppPreferences.operativeGuideUrl)
        }
    }

    private fun setupRecyclerView() {
        binding.toursRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.toursRecyclerView.setHasFixedSize(true)
        binding.videosRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        binding.videosRecyclerView.setHasFixedSize(true)
    }

}
