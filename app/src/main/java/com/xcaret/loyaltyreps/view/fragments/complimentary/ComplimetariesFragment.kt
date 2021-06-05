package com.xcaret.loyaltyreps.view.fragments.complimentary


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.snackbar.Snackbar
import com.xcaret.loyaltyreps.MainActivity
import org.json.JSONException
import org.json.JSONObject

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.adapter.XComplimentaryAdapter
import com.xcaret.loyaltyreps.database.XCaretLoyaltyDatabase
import com.xcaret.loyaltyreps.databinding.FragmentComplimetariesBinding
import com.xcaret.loyaltyreps.model.Complimentary
import com.xcaret.loyaltyreps.model.XComplimentary
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 *
 */
class ComplimetariesFragment : Fragment() {

    lateinit var binding: FragmentComplimetariesBinding
    lateinit var xUserViewModel: XUserViewModel

    val ENDPOINT_COMPLIMETARIES = "complimentaries/"
    lateinit var mAdapter: XComplimentaryAdapter
    private var mComplimentaries: ArrayList<Complimentary> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_complimetaries, container, false)

        val mApplication = requireNotNull(this.activity).application
        val dataSource = XCaretLoyaltyDatabase.getInstance(mApplication).loyaltyDatabaseDAO
        val viewModelFactory = XUserViewModelFactory(dataSource, mApplication)
        xUserViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(XUserViewModel::class.java)
        binding.lifecycleOwner = this

        loadViews()

        //println("user token ... ${AppPreferences.userToken}")

        return binding.root
    }

    private fun loadViews(){
        xUserViewModel.currentXUser.observe(this, Observer {
                xuser ->
                xuser?.let {
                    if (it.cnMainQuiz && it.estatus && it.idEstatusArchivos == 3){
                        setupRecyclerView()

                        loadCompliemtaries()
                   } else {
                        findNavController().navigate(R.id.moduleNotAvailableFragment)
                   }
                }
        })
    }

    private fun setupRecyclerView() {
        binding.complimentariesRV.setHasFixedSize(true)
        binding.complimentariesRV.layoutManager = LinearLayoutManager(activity!!,
            RecyclerView.VERTICAL, false)

        mAdapter = XComplimentaryAdapter(activity!!, activity, mComplimentaries)

        binding.complimentariesRV.adapter = mAdapter
    }

    private fun loadCompliemtaries(){

        mComplimentaries.clear()
        val jsonObject = JSONObject()
        try {
            jsonObject.put("card", AppPreferences.userRCX)
            jsonObject.put("token", AppPreferences.userToken)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        println("jsonobjectrequescomplimentary $jsonObject")

        AndroidNetworking.post(AppPreferences.PUNK_API_URL+ENDPOINT_COMPLIMETARIES)
            .addJSONObjectBody(jsonObject) // posting json
            .setPriority(Priority.LOW)
            .addHeaders("Authorization", "token "+ AppPreferences.PUNK_API_TOKEN)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onError(anError: ANError?) {
                    if (anError!!.errorCode == 401 || anError.errorCode == 400) {
                        println("myresponse detail ${anError.errorCode}")
                        println("myresponse detail ${anError.errorBody}")
                        println("myresponse detail ${anError.response}")
                        //val mActivity = activity as MainActivity?
                        //mActivity!!.xUserLogout(activity!!, "La sesión ha caducado", "Vuelve a iniciar sesión")
                    }
                    binding.runoutOfItems.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    /*println("myresponse detail $anError")
                    println("myresponse detail ${anError!!.errorCode}")
                    println("myresponse detail ${anError.errorDetail}")
                    if (anError.errorDetail == "connectionError"){
                        binding.runoutOfItems.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                    }*/
                }
                override fun onResponse(response: JSONObject?) {
                    println("myresponse $response")
                    try {
                        for (item in 0 until response!!.getJSONArray("value").length()){
                            val xcom_item = response.getJSONArray("value").getJSONObject(item)

                            val noTarjeta = if (xcom_item.has("noTarjeta")){
                                if (xcom_item.get("noTarjeta").toString() == "null") {
                                    ""
                                } else {
                                    xcom_item.getString("noTarjeta")
                                }
                            } else { "" }

                            val nombreAfiliado = if (xcom_item.has("nombreAfiliado")){
                                if (xcom_item.get("nombreAfiliado").toString() == "null") {
                                    ""
                                } else {
                                    xcom_item.getString("nombreAfiliado")
                                }
                            } else { "" }

                            val tarjetaEspecial = if (xcom_item.has("tarjetaEspecial")){
                                if (xcom_item.get("tarjetaEspecial").toString() == "null") {
                                    ""
                                } else {
                                    xcom_item.getString("tarjetaEspecial")
                                }
                            } else { "" }

                            val parque = if (xcom_item.has("parque")){
                                if (xcom_item.get("parque").toString() == "null") {
                                    ""
                                } else {
                                    xcom_item.getString("parque")
                                }
                            } else { "" }

                            val idServicio = if (xcom_item.has("idServicio")){
                                if (xcom_item.get("idServicio").toString() == "null") {
                                    ""
                                } else {
                                    xcom_item.getString("idServicio")
                                }
                            } else { "" }

                            val servicio = if (xcom_item.has("servicio")){
                                if (xcom_item.get("servicio").toString() == "null") {
                                    ""
                                } else {
                                    xcom_item.getString("servicio")
                                }
                            } else { "" }

                            val noPaxBeneficio = if (xcom_item.has("noPaxBeneficio")){
                                if (xcom_item.get("noPaxBeneficio").toString() == "0") {
                                    0
                                } else {
                                    xcom_item.getInt("noPaxBeneficio")
                                }
                            } else { 0 }

                            val noPaxUtilizado = if (xcom_item.has("noPaxUtilizado")){
                                if (xcom_item.get("noPaxUtilizado").toString() == "0") {
                                    0
                                } else {
                                    xcom_item.getInt("noPaxUtilizado")
                                }
                            } else { 0 }

                            val noPaxPorUtilizar = if (xcom_item.has("noPaxPorUtilizar")){
                                if (xcom_item.get("noPaxPorUtilizar").toString() == "0") {
                                    0
                                } else {
                                    xcom_item.getInt("noPaxPorUtilizar")
                                }
                            } else { 0 }

                            val mimage = if (xcom_item.has("image")) xcom_item.getString("image") else ""

                            val name = if (xcom_item.has("name")){
                                if (xcom_item.get("name").toString() == "null") {
                                    ""
                                } else {
                                    xcom_item.getString("name")
                                }
                            } else { "" }

                            val phone = if (xcom_item.has("phone")){
                                if (xcom_item.get("phone").toString() == "null") {
                                    ""
                                } else {
                                    xcom_item.getString("phone")
                                }
                            } else { "" }

                            val action = if (xcom_item.has("action")){
                                if (xcom_item.get("action").toString() == "null") {
                                    ""
                                } else {
                                    xcom_item.getString("action")
                                }
                            } else { "" }

                            val infants = if (xcom_item.has("infants")){
                                if (xcom_item.get("infants").toString() == "false") {
                                    false
                                } else {
                                    xcom_item.getBoolean("infants")
                                }
                            } else { false }

                            val note = if (xcom_item.has("note")){
                                if (xcom_item.get("note").toString() == "null") {
                                    ""
                                } else {
                                    xcom_item.getString("note")
                                }
                            } else { "" }

                            val order = if (xcom_item.has("order")){
                                if (xcom_item.get("order").toString() == "0") {
                                    0
                                } else {
                                    xcom_item.getInt("order")
                                }
                            } else { 0 }
                            //val tarjetaEspecial = if (xcom_item.get("tarjetaEspecial").toString() == "null") "" else xcom_item.getString("nombreAfiliado")
                            //val mphone = if (xcom_item.get("phone").toString() == "null") "" else xcom_item.getString("phone")


                            mComplimentaries.add(
                                Complimentary(
                                    noTarjeta,
                                    nombreAfiliado,
                                    tarjetaEspecial,
                                    parque,
                                    idServicio,
                                    servicio,
                                    noPaxBeneficio,
                                    noPaxUtilizado,
                                    noPaxPorUtilizar,
                                    mimage,
                                    name,
                                    phone,
                                    action,
                                    infants,
                                    note,
                                    order
                                )
                            )

                        }
                        binding.complimentariesRV.adapter!!.notifyDataSetChanged()
                        binding.progressBar.visibility = View.GONE
                    } catch (except: Exception) {
                        except.printStackTrace()
                    }
                }
            })
    }

    private fun snackBarMessage(apiResponse: String){
        Snackbar.make(binding.mainContainer,
            apiResponse,
            Snackbar.LENGTH_LONG)
            .show()
        binding.progressBar.visibility = View.GONE
    }
}
