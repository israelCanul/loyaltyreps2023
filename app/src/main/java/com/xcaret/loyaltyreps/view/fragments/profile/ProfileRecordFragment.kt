package com.xcaret.loyaltyreps.view.fragments.profile


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
import com.xcaret.loyaltyreps.databinding.FragmentProfileRecordBinding
import com.xcaret.loyaltyreps.model.XUserPuntoPos
import com.xcaret.loyaltyreps.model.XUserPuntosNeg
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory
import kotlinx.android.synthetic.main.pickup_schedule_table_row.view.*

/**
 * A simple [Fragment] subclass.
 *
 */
class ProfileRecordFragment : Fragment() {

    lateinit var binding: FragmentProfileRecordBinding
    lateinit var xUserViewModel: XUserViewModel

    private var listOfPositivePoints: ArrayList<XUserPuntoPos> = ArrayList()
    private var positivePointsAdapter: XPuntosAdapter? = null

//    private var listOfNegativePoints: ArrayList<XUserPuntosNeg> = ArrayList()
//    private var negativePointsAdapter: XPuntosNegAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_record, container, false)

        val mApplication = requireNotNull(this.activity).application
        //reference to the DAO
        val dataSource = XCaretLoyaltyDatabase.getInstance(mApplication).loyaltyDatabaseDAO
        //create instance
        val viewModelFactory = XUserViewModelFactory(dataSource, mApplication)
        xUserViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(XUserViewModel::class.java)
        binding.lifecycleOwner = this

        loadRecyclerView()
        loadTopTenAsignationPoints()
//        loadOperacionesCanje()

        binding.positiveRecordButton.performClick()

        return binding.root
    }

    private fun loadRecyclerView(){
        binding.recordButtonsRecyclerView.setHasFixedSize(true)
        binding.recordButtonsRecyclerView.layoutManager = LinearLayoutManager(
            activity!!, RecyclerView.VERTICAL, false)

        positivePointsAdapter = XPuntosAdapter(activity!!, listOfPositivePoints)
        binding.recordButtonsRecyclerView.adapter = positivePointsAdapter
//        negativePointsAdapter = XPuntosNegAdapter(activity!!, listOfNegativePoints)


//        binding.positiveRecordButton.setOnClickListener {
//            binding.recordButtonsRecyclerView.adapter = positivePointsAdapter
//            binding.negativeRecordButton.background = ContextCompat.getDrawable(activity!!, R.drawable.button_green_border_bgtransparent)
//            binding.negativeRecordButton.setTextColor(Color.parseColor("#67a33c"))
//            it.background = ContextCompat.getDrawable(activity!!, R.drawable.button_green)
//            binding.positiveRecordButton.setTextColor(Color.WHITE)
//        }
//
//        binding.negativeRecordButton.setOnClickListener {
//            binding.recordButtonsRecyclerView.adapter = negativePointsAdapter
//            it.background = ContextCompat.getDrawable(activity!!, R.drawable.button_green)
//            binding.negativeRecordButton.setTextColor(Color.WHITE)
//            binding.positiveRecordButton.background = ContextCompat.getDrawable(activity!!, R.drawable.button_green_border_bgtransparent)
//            binding.positiveRecordButton.setTextColor(Color.parseColor("#67a33c"))
//        }

        binding.gotoQuiz.setOnClickListener {
            findNavController().navigate(R.id.actionXHome)
        }

    }

    private fun loadTopTenAsignationPoints(){
        listOfPositivePoints.clear()
        val jsonObject = JSONObject()
        try {
            jsonObject.put("idRep", AppPreferences.idRep)
            jsonObject.put("idUsuario", AppPreferences.idUsuaro)
            jsonObject.put("ip", "")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        AndroidNetworking.post(AppPreferences.getTopTenAsignacionPuntos)
            .addJSONObjectBody(jsonObject)
            .addHeaders("Authorization", "Bearer "+AppPreferences.userToken)
            .setTag("user_history")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onError(anError: ANError?) {
                    if (anError!!.errorCode == 401) {
                        val mActivity = activity as MainActivity?
                        mActivity!!.xUserLogout(activity!!, "La sesión ha caducado", "Vuelve a iniciar sesión")
                    }
                    if(anError!!.errorCode == 204){// si el endpoint no devuelve info
                        binding.NoRecords.visibility = View.VISIBLE
                    }
                    hideButtonsAndListContainer()
                }
                override fun onResponse(response: JSONObject) {

                    try {

                        if (response.getJSONArray("value").length() > 0) {
                            binding.NoRecords.visibility = View.GONE
                            for (item in 0 until response.getJSONArray("value").length()) {
                                val mitem = response.getJSONArray("value").getJSONObject(item)
                                listOfPositivePoints.add(
                                    XUserPuntoPos(
                                        mitem.getInt("idAsignacionPuntos"),
                                        mitem.getInt("idRep"),
                                        mitem.getInt("idUsuario"),
                                        mitem.getString("fecha"),
                                        mitem.getInt("puntos"),
                                        mitem.getString("comentario")
                                    )
                                )
                            }

                            binding.recordButtonsRecyclerView.adapter!!.notifyDataSetChanged()

                        }else{// si no hay records
                            binding.NoRecords.visibility = View.VISIBLE
                        }
                    } catch (except: Exception) {
                        except.printStackTrace()
                    }
                }
            })
    }

//    private fun loadOperacionesCanje(){
//        listOfPositivePoints.clear()
//        val jsonObject = JSONObject()
//        try {
//            jsonObject.put("idRep", AppPreferences.idRep)
//            jsonObject.put("idUsuario", AppPreferences.idUsuaro)
//            jsonObject.put("ip", "")
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//
//        AndroidNetworking.post(AppPreferences.getOperacionesCanje)
//            .addJSONObjectBody(jsonObject)
//            .addHeaders("Authorization", "bearer "+AppPreferences.userToken)
//            .setTag("user_history")
//            .setPriority(Priority.HIGH)
//            .build()
//            .getAsJSONObject(object : JSONObjectRequestListener {
//                override fun onError(anError: ANError?) {
//                    println("ahahahah ${anError!!.errorCode}")
//                }
//                override fun onResponse(response: JSONObject) {
//                    println("record response $response")
//                    if (response.getJSONArray("value").length() > 0) {
//                        for (item in 0 until response.getJSONArray("value").length()) {
//                            val mitem = response.getJSONArray("value").getJSONObject(item)
//                            listOfNegativePoints.add(
//                                XUserPuntosNeg(
//                                    mitem.getInt("idOperacion"),
//                                    mitem.getInt("idEdoOperacion"),
//                                    mitem.getString("fecha"),
//                                    mitem.getString("ip"),
//                                    mitem.getInt("puntos"),
//                                    mitem.getString("articulo")
//                                )
//                            )
//                        }
//
//                        binding.recordButtonsRecyclerView.adapter!!.notifyDataSetChanged()
//
//                    } else {
//                        binding.beganTraining.visibility = View.VISIBLE
//                        hideButtonsAndListContainer()
//                    }
//                }
//            })
//    }

    private fun hideButtonsAndListContainer(){
        binding.recordButtons.visibility = View.GONE
        binding.listContainer.visibility = View.GONE
    }


}
