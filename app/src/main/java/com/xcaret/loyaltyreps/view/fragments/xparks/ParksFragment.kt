package com.xcaret.loyaltyreps.view.fragments.xparks


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import org.json.JSONArray
import com.xcaret.loyaltyreps.R
//import punkmkt.com.xcaretloyaltyr.adapter.ClickListener
import com.xcaret.loyaltyreps.adapter.XParkAdapter
import com.xcaret.loyaltyreps.databinding.FragmentParksBinding
import com.xcaret.loyaltyreps.model.XPark
import com.xcaret.loyaltyreps.model.XParkInfographic
import com.xcaret.loyaltyreps.util.AppPreferences

class ParksFragment : Fragment() {

    lateinit var binding: FragmentParksBinding

    private val ENDPOINT_PARKS = "parks/"
    private val clickListener: (XPark) -> Unit = this::onXParkClicked
    private var recyclerViewAdapter: XParkAdapter? = null
    private var xParksList: ArrayList<XPark> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_parks, container, false)

        setupRecyclerView()
        loadXParks()

        return binding.root
    }

    private fun loadXParks(){
        xParksList.clear()
        AndroidNetworking.get(AppPreferences.PUNK_API_URL+ENDPOINT_PARKS)
            .setPriority(Priority.LOW)
            .addHeaders("Authorization", "token "+AppPreferences.PUNK_API_TOKEN)
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
                            val xinfographics: ArrayList<XParkInfographic> = ArrayList()
                            val mPark = XPark(
                                xpark_item.getInt("id"),
                                xpark_item.getString("name"),
                                xpark_item.getString("logo"),
                                xpark_item.getString("color"),
                                xinfographics
                            )
                            xParksList.add(mPark)
                            AppPreferences.xParksList.add(mPark)
                        }
                        recyclerViewAdapter = XParkAdapter(/*clickListener, */1,activity!!, R.layout.cardview_park, xParksList)
                        binding.parkRecyclerView.adapter = recyclerViewAdapter
                        binding.parkRecyclerView.adapter!!.notifyDataSetChanged()
                    } catch (excep: Exception) {
                        excep.printStackTrace()
                    }
                }
            })
    }

    private fun onXParkClicked(xPark: XPark){
        //Toast.makeText(activity, "parkClicked---"+xPark.name, Toast.LENGTH_SHORT).show()
        val bundle = Bundle().also {
            it.putString("xpark_name", xPark.name)
            it.putString("xpark_id", xPark.id.toString())
        }
        findNavController().navigate(R.id.to_parkDetailsFragment, bundle)
    }

    private fun setupRecyclerView() {
        binding.parkRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.parkRecyclerView.setHasFixedSize(true)
    }

}
