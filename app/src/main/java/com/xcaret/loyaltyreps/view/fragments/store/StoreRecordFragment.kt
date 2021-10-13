package com.xcaret.loyaltyreps.view.fragments.store


import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONException
import org.json.JSONObject
import com.xcaret.loyaltyreps.MainActivity

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.adapter.XPuntosAdapter
import com.xcaret.loyaltyreps.adapter.XPuntosNegAdapter
import com.xcaret.loyaltyreps.database.XCaretLoyaltyDatabase
import com.xcaret.loyaltyreps.databinding.FragmentStoreRecordBinding
import com.xcaret.loyaltyreps.model.XUserPuntoPos
import com.xcaret.loyaltyreps.model.XUserPuntosNeg
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory

/**
 * A simple [Fragment] subclass.
 *
 */
class StoreRecordFragment : Fragment() {
    lateinit var binding: FragmentStoreRecordBinding
    lateinit var xUserViewModel: XUserViewModel

    private var listOfNegativePoints: ArrayList<XUserPuntosNeg> = ArrayList()
    private var negativePointsAdapter: XPuntosNegAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_store_record, container, false)

        val mApplication = requireNotNull(this.activity).application
        //reference to the DAO
        val dataSource = XCaretLoyaltyDatabase.getInstance(mApplication).loyaltyDatabaseDAO
        //create instance
        val viewModelFactory = XUserViewModelFactory(dataSource, mApplication)
        xUserViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(XUserViewModel::class.java)
        binding.lifecycleOwner = this

        loadRecyclerView()
        loadOperacionesCanje()

        return binding.root
    }

    private fun loadRecyclerView(){
        binding.recordButtonsRecyclerView.setHasFixedSize(true)
        binding.recordButtonsRecyclerView.layoutManager = LinearLayoutManager(
            activity!!, RecyclerView.VERTICAL, false)
        negativePointsAdapter = XPuntosNegAdapter(activity!!, listOfNegativePoints)
        binding.recordButtonsRecyclerView.adapter = negativePointsAdapter
    }


    private fun loadOperacionesCanje(){

        val jsonObject = JSONObject()
        try {
            jsonObject.put("idRep", AppPreferences.idRep)
            jsonObject.put("idUsuario", AppPreferences.idUsuaro)
            jsonObject.put("ip", "")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        AndroidNetworking.post(AppPreferences.getOperacionesCanje2)
            .addJSONObjectBody(jsonObject)
            .addHeaders("Authorization", "bearer "+AppPreferences.userToken)
            .setTag("user_history")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onError(anError: ANError?) {
                    println("ahahahah ${anError!!.errorCode}")
                    binding.NoRecords.visibility = View.VISIBLE
                    hideListContainerIfNoRecords()
                }
                override fun onResponse(response: JSONObject) {
                    println("record response $response")
                    if (response.getJSONArray("value").length() > 0) {
                        for (item in 0 until response.getJSONArray("value").length()) {
                            binding.beganTraining.visibility = View.GONE
                            val mitem = response.getJSONArray("value").getJSONObject(item)
                            println("resultados: $mitem")
                            listOfNegativePoints.add(
                                XUserPuntosNeg(
                                    mitem.getInt("idOperacion"),
                                    mitem.getInt("idEdoOperacion"),
                                    mitem.getString("fecha"),
                                    mitem.getString("ip"),
                                    mitem.getInt("puntos"),
                                    mitem.getString("articulo"),
                                    mitem.getInt("idEstatus"),
                                    mitem.getString("estatus"),
                                    mitem.getString("observaciones")
                                )
                            )
                        }
                        binding.recordButtonsRecyclerView.adapter!!.notifyDataSetChanged()

                    } else {
                        binding.NoRecords.visibility = View.VISIBLE
                        hideListContainerIfNoRecords()
                    }
                }
            })
    }

    private fun hideListContainerIfNoRecords(){

    }


}
