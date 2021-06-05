package com.xcaret.loyaltyreps.view.fragments

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.android.synthetic.main.fragment_pick_ups.*
import org.json.JSONException
import org.json.JSONObject

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.adapter.XHotelAdapter
import com.xcaret.loyaltyreps.databinding.FragmentPickUpsBinding
import com.xcaret.loyaltyreps.model.PickUpHotel
import com.xcaret.loyaltyreps.model.XTour
import com.xcaret.loyaltyreps.model.XTourSchedule
import com.xcaret.loyaltyreps.model.XZone
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions
import java.util.*
import kotlin.collections.ArrayList

class PickUpsFragment : Fragment() {

    lateinit var binding: FragmentPickUpsBinding
    lateinit var mView: View

    lateinit var zoneSpinnerAdapter: ArrayAdapter<XZone>
    lateinit var tourSpinnerAdapter: ArrayAdapter<XTour>
    lateinit var scheduleSpinnerAdapter: ArrayAdapter<XTourSchedule>
    lateinit var searchViewAdapter: XHotelAdapter

    var pickupsList: ArrayList<PickUpHotel> = ArrayList()

    var idProducto: String = ""
    var fechaVisita: String = ""
    var idUbicacionGeografica: String = ""
    var idUnidadNegocio: String = ""
    var cnAllotmentHorario: String = ""
    var horario: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_pick_ups, container, false)

        //mView = binding.root

        return  binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loadTours()
        loadZones()
        loadResources()

        searchHotelSchedule()

    }

    private fun loadResources(){
        populateTourSpinner()
    }

    private fun loadTours(){
        AppPreferences.xTourList.clear()
        AppPreferences.xTourList.add(
            XTour(0,"Selecciona un tour", "ST",
                true,0,0
            )
        )
        AndroidNetworking.get(AppPreferences.XCARET_API_URL+"getTours")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    val listOfTours = response.getJSONArray("value")
                    for (item in 0 until listOfTours.length()){
                        val mTour = listOfTours.getJSONObject(item)
                        AppPreferences.xTourList.add(
                            XTour(
                                mTour.getInt("idProducto"),
                                mTour.getString("dsDescripcion"),
                                mTour.getString("dsClave"),
                                mTour.getBoolean("cnAllotmentHorario"),
                                mTour.getInt("idLocacion"),
                                mTour.getInt("idUnidadNegocio")
                            )
                        )
                    }
                    tourNotifyAdapter()
                }

                override fun onError(error: ANError) {

                    println("eeeeeooo${error.errorDetail}")
                }
            })
    }

    private fun loadZones(){
        AppPreferences.xZoneList.clear()
        AppPreferences.xZoneList.add(
            XZone(0, "SZ", "Selecciona una zona", true,
                "", 0, 0))
        AndroidNetworking.get(AppPreferences.XCARET_API_URL+"getUbicacionGeografica")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    val listOfZones = response.getJSONArray("value")
                    for (item in 0 until listOfZones.length()){
                        val mZone = listOfZones.getJSONObject(item)
                        AppPreferences.xZoneList.add(
                            XZone(
                                mZone.getInt("idUbicacionGeografica"),
                                mZone.getString("dsClave"),
                                mZone.getString("dsUbicacionGeografica"),
                                mZone.getBoolean("cnActivo"),
                                mZone.getString("feAlta"),
                                mZone.getInt("idClienteUsuarioAlta"),
                                mZone.getInt("prIVA")
                            )
                        )
                    }
                    notifyAdapter()
                }
                override fun onError(error: ANError) {
                    println("eeeeeooo${error.errorDetail}")
                }
            })
    }

    private fun loadXTourSchedule(){
        val horarios: ArrayList<XTourSchedule> = ArrayList()
        horarios.clear()

        horarios.add(XTourSchedule(0, "Selecciona un horario"))

        scheduleSpinnerAdapter = ArrayAdapter(activity!!, R.layout.spinner_item, horarios)
        binding.selectSchedule.adapter = scheduleSpinnerAdapter
        binding.selectSchedule.onItemSelectedListener = scheduleSpinnerListener

        populateHotelPickUpSchedules()

        val jsonObject = JSONObject()
        try {
            jsonObject.put("idProducto", idProducto)
            jsonObject.put("fechaVisita", fechaVisita)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        AndroidNetworking.post(AppPreferences.XCARET_API_URL+"getHorariosProducto")
            .addJSONObjectBody(jsonObject) // posting json
            .setTag("getTourHorarios")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    val tourSchedules = response.getJSONArray("value")

                    for (item in 0 until tourSchedules.length()){
                        val horario = tourSchedules.getJSONObject(item)
                        horarios.add(
                            XTourSchedule(horario.getInt("retValue"), horario.getString("horario"))
                        )
                    }
                    scheduleNotifyAdapter()
                }

                override fun onError(anError: ANError?) {
                    println("eeerrrror${anError!!.errorDetail}")
                }

            })
    }

    private fun populateTourSpinner(){
        zoneSpinnerAdapter = ArrayAdapter(activity!!, R.layout.spinner_item, AppPreferences.xZoneList)
        binding.selectZone.adapter = zoneSpinnerAdapter
        binding.selectZone.onItemSelectedListener = zoneSpinnerListener

        tourSpinnerAdapter = ArrayAdapter(activity!!, R.layout.spinner_item, AppPreferences.xTourList)
        binding.selectLanguage.adapter = tourSpinnerAdapter
        binding.selectLanguage.onItemSelectedListener = tourSpinnerListener

        binding.selectDate.setOnClickListener{
            selectDate()
        }
    }

    private fun notifyAdapter() {
        try {
            activity!!.runOnUiThread(Runnable {
                zoneSpinnerAdapter.notifyDataSetChanged()
            })
        } catch (ex: Exception){
            ex.printStackTrace()
        }
    }

    private fun tourNotifyAdapter() {
        try {
            activity!!.runOnUiThread(Runnable {
                tourSpinnerAdapter.notifyDataSetChanged()
            })
        } catch (ex: Exception){
            ex.printStackTrace()
        }
    }

    private fun scheduleNotifyAdapter() {
        try {
            activity!!.runOnUiThread(Runnable {
                scheduleSpinnerAdapter.notifyDataSetChanged()
            })
        } catch (ex: Exception){
            ex.printStackTrace()
        }
    }

    private val zoneSpinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val xZone = parent.selectedItem as XZone
            idUbicacionGeografica = xZone.idUbicacionGeografica.toString()

            populateHotelPickUpSchedules()
        }
        override fun onNothingSelected(parent: AdapterView<*>) {
        }
    }

    private val tourSpinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val xTour = parent.selectedItem as XTour
            idProducto = xTour.idProducto.toString()
            idUnidadNegocio = xTour.idUnidadNegocio.toString()
            cnAllotmentHorario = xTour.cnAllotmentHorario.toString()

            if (xTour.cnAllotmentHorario && xTour.idProducto != 0) {
                binding.horario.visibility = View.VISIBLE
                loadXTourSchedule()
                populateHotelPickUpSchedules()
            } else {
                binding.horario.visibility = View.GONE
                populateHotelPickUpSchedules()
            }
        }
        override fun onNothingSelected(parent: AdapterView<*>) {
        }
    }

    private val scheduleSpinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val xSchedule = parent.selectedItem as XTourSchedule
            horario = xSchedule.horario

            populateHotelPickUpSchedules()
        }
        override fun onNothingSelected(parent: AdapterView<*>) {
        }
    }

    private fun selectDate(){
        val c = Calendar.getInstance()
        val cyear = c.get(Calendar.YEAR)
        val cmonth = c.get(Calendar.MONTH)
        val cday = c.get(Calendar.DAY_OF_MONTH)
        val date_now = System.currentTimeMillis() - 1000
        val datePickerDialog = DatePickerDialog(activity!!,
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                var format = ""
                if (month + 1 < 10){
                    format = "0"
                }
                var format1 = ""
                if (day < 10){
                    format1 = "0"
                }
                val finalDate = format1 + day.toString() +" - " + format + (month+1).toString() + " - " + year.toString()
                binding.selectedDate.text = finalDate
                binding.selectedDate.setTextColor(Color.parseColor("#000000"))
                fechaVisita = year.toString() + format + (month+1).toString() + format1 + day.toString()

                if (cnAllotmentHorario.equals("true") && idProducto.toInt() != 0) {
                    binding.horario.visibility = View.VISIBLE
                    loadXTourSchedule()
                    populateHotelPickUpSchedules()
                } else {
                    binding.horario.visibility = View.GONE
                    populateHotelPickUpSchedules()
                }
                //populateHotelPickUpSchedules()

            }, cyear, cmonth, cday)

        datePickerDialog.datePicker.minDate = date_now + 1000*60*60*24*1
        datePickerDialog.setCanceledOnTouchOutside(false)
        datePickerDialog.show()
    }


    private fun populateHotelPickUpSchedules(){
        pickupsList.clear()
        binding.pickupsRecyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        searchViewAdapter = XHotelAdapter(activity!!, pickupsList)
        binding.pickupsRecyclerView.adapter = searchViewAdapter

        val jsonObject = JSONObject()
        try {
            jsonObject.put("idProducto", idProducto)
            jsonObject.put("fechaVisita", fechaVisita)
            jsonObject.put("idUbicacionGeografica", idUbicacionGeografica)
            jsonObject.put("idUnidadNegocio", idUnidadNegocio)
            jsonObject.put("cnAllotmentHorario", cnAllotmentHorario)
            jsonObject.put("horario", horario)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        EventsTrackerFunctions.trackPickupEvent(jsonObject.toString())

        AndroidNetworking.post(AppPreferences.XCARET_API_URL+"getPickupHotel")
            .addJSONObjectBody(jsonObject) // posting json
            .setTag("getPickupHotel")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    val listOfPickupSchedules = response.getJSONArray("value")
                    if (listOfPickupSchedules.length() > 0) {
                        binding.resultsEmpty.visibility = View.GONE
                        for (item in 0 until listOfPickupSchedules.length()){
                            val pickupSchedule = listOfPickupSchedules.getJSONObject(item)
                            pickupsList.add(
                                PickUpHotel(
                                    pickupSchedule.getInt("idHotel"),
                                    pickupSchedule.getString("dsNombreHotel"),
                                    pickupSchedule.getInt("idHotelPickupHorarioProducto"),
                                    pickupSchedule.getString("hrPickup")
                                )
                            )
                        }
                        binding.pickupsRecyclerView.adapter!!.notifyDataSetChanged()
                    } else {
                        binding.resultsEmpty.visibility = View.VISIBLE
                    }
                }

                override fun onError(anError: ANError?) {
                    println("eeerrrror"+anError)
                }

            })

    }

    private fun searchHotelSchedule(){
        searchHotel.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchViewAdapter.filter.filter(newText)
                return false
            }

        })
    }

}
