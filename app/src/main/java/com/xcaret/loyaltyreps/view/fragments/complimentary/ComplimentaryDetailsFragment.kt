package com.xcaret.loyaltyreps.view.fragments.complimentary


//import android.app.DatePickerDialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.whenCreated
import androidx.navigation.fragment.findNavController

import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.database.XCaretLoyaltyDatabase
import com.xcaret.loyaltyreps.databinding.FragmentComplimentaryDetailsBinding
import com.xcaret.loyaltyreps.model.Complimentary
import com.xcaret.loyaltyreps.model.Hijo
import com.xcaret.loyaltyreps.model.XUser
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions
import com.xcaret.loyaltyreps.util.FormManagerComplimentary
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.function.BooleanSupplier
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 *
 */
class ComplimentaryDetailsFragment : Fragment() {

    lateinit var binding: FragmentComplimentaryDetailsBinding
    lateinit var xUserViewModel: XUserViewModel


    var adultSpinnerAdapter: ArrayAdapter<Hijo>? = null
    var kidSpinnerAdapter: ArrayAdapter<Hijo>? = null
    var infantSpinnerAdapter: ArrayAdapter<Hijo>? = null

    var complimentaryTem: Complimentary? = null
    private var adulstList: ArrayList<Hijo> = ArrayList()
    private var kidsList: ArrayList<Hijo> = ArrayList()
    private var infantsList: ArrayList<Hijo> = ArrayList()
    var maxKids = 0
    var maxInfants = 6
    var noAdults = 1
    var nameAdults: JSONArray? = JSONArray()
    var noKids = 0
    var nameKids: JSONArray? = JSONArray()
    var noInfants = 0
    var nameInfants: JSONArray? = JSONArray()
    var diasBL = ArrayList<String>()
    var formManager = FormManagerComplimentary()

    var fechaVisita: String = ""
    var fullName: String = ""
    var repName = ""
    var repLastP = ""
    var repLastM = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_complimentary_details, container, false)

        val mApplication = requireNotNull(this.activity).application
        val dataSource = XCaretLoyaltyDatabase.getInstance(mApplication).loyaltyDatabaseDAO
        val viewModelFactory = XUserViewModelFactory(dataSource, mApplication)
        xUserViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(XUserViewModel::class.java)
        binding.lifecycleOwner = this
        try {
            complimentaryTem = arguments!!.getParcelable("complimentary") as Complimentary?
            loadDataFromDao()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return binding.root
    }

    private fun loadDataFromDao(){
        xUserViewModel.currentXUser.observe(viewLifecycleOwner, Observer {
                xuser ->
            xuser?.let {
                populateInfo(it)
            }
        })
    }
    fun getDataFromFirebase(idServicio: String) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("servicios/" + idServicio+"/blackListDays")

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                println("DataBaseChanges " + p0.value)
                if(p0.value !== null){
                   var dias = p0.value
                   dias = dias.toString().split(",")
                   diasBL = dias as ArrayList<String>
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun populateInfo(xUser: XUser){
        println("complimentary " + complimentaryTem)
        binding.complimentaryTitle.text = complimentaryTem!!.name
        fullName = "${xUser.nombre} ${xUser.apellidoPaterno} ${xUser.apellidoMaterno}"
        repName = xUser.nombre
        repLastP = xUser.apellidoPaterno
        repLastM = xUser.apellidoMaterno
        binding.reservationUser.setText(fullName)
        binding.reservationPark.setText(complimentaryTem!!.name)
        binding.reservationAgency.setText(xUser.agencia)
        binding.reservationRCX.setText(xUser.correo)

        complimentaryTem!!.idServicio?.let { getDataFromFirebase(it) }
//        if (!complimentaryTem!!.infants){
//            binding.textView16.visibility = View.GONE
//        }

        loadNumberOfAdults()
        loadNumberOfInfants()
        handleClick(xUser)
    }

    private fun handleClick(xUser: XUser){
        binding.reservationDate.setOnClickListener { selectDate() }

        binding.requestReservation.setOnClickListener {
            sendReservationRequest(xUser)
        }
    }

    private fun loadNumberOfAdults(){
        adulstList.clear()
        //for (item in 0 until complimentaryTem!!.noPaxBeneficio + 1) {
        for (item in 1 until complimentaryTem!!.noPaxPorUtilizar + 1) {
            adulstList.add(
                Hijo(item, item.toString())
            )
        }
        notifyAdultAdapter()

        adultSpinnerAdapter = ArrayAdapter(activity!!, R.layout.spinner_item, adulstList)
        binding.numberOfAdults.adapter = adultSpinnerAdapter
        binding.numberOfAdults.onItemSelectedListener = adultSpinnerListener
    }

    private val adultSpinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            try{

                val hij = parent.selectedItem as Hijo
                noAdults = hij.desc.toInt()
                if(noAdults>0){
                    binding.labelVisitersAdults.visibility = TextView.VISIBLE
                }else{
                    binding.labelVisitersAdults.visibility = TextView.GONE
                }
                binding.namesAdults?.removeAllViews()

                for (i in 0 until noAdults){
                    binding.namesAdults?.addView(formManager.CreateAdultOnList(i,activity,context,binding,repName,repLastP,repLastM))
                }
                maxKids = (complimentaryTem!!.noPaxPorUtilizar + 1) - noAdults

                if (maxKids > 0) {
                    loadNumberOfKids()
                }

            } catch (error: java.lang.Exception) {
                error.printStackTrace()
            }
        }
        override fun onNothingSelected(parent: AdapterView<*>) {
        }
    }

    private fun notifyAdultAdapter(){
        try {
            activity!!.runOnUiThread(Runnable {
                adultSpinnerAdapter!!.notifyDataSetChanged()
            })
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    private fun loadNumberOfKids(){
        kidsList.clear()
        for (item in 0 until maxKids) {
            kidsList.add(
                Hijo(item, item.toString())
            )
        }

        notifyKidsAdapter()


        kidSpinnerAdapter = ArrayAdapter(activity!!, R.layout.spinner_item, kidsList)
        binding.numberOfKids.adapter = kidSpinnerAdapter
        binding.numberOfKids.onItemSelectedListener = kidSpinnerListener
    }

    private val kidSpinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            try{
                val kid = parent.selectedItem as Hijo
                noKids = kid.desc.toInt()

                // creating the list of children´s view
                if(noKids>0){
                    binding.labelVisitersChildren.visibility = TextView.VISIBLE
                }else{
                    binding.labelVisitersChildren.visibility = TextView.GONE
                }
                binding.namesChildren?.removeAllViews()
                for (i in 0 until noKids){
//                    var textInputLayout = TextInputLayout(context)
//                    val lp = LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    lp.setMargins(0, 0, 0, 0)
//                    textInputLayout.layoutParams = lp
//                    var inputText = TextInputEditText(ContextThemeWrapper(activity, R.style.CInput))
//                    inputText.setHint("Nombre y Apellido (Niño "+(i+1)+")")
//                    inputText.isEnabled = true
//                    textInputLayout.addView(inputText)
//                    binding.namesChildren?.addView(textInputLayout)
                    binding.namesChildren?.addView(formManager.CreateInfantesList(i,activity,context,binding))
                }

                //the list of infants always have 6 elements [0-5]
                //loadNumberOfInfants()

            } catch (error: java.lang.Exception) {
                error.printStackTrace()
            }
        }
        override fun onNothingSelected(parent: AdapterView<*>) {
        }
    }

    private fun notifyKidsAdapter(){
        try {
            activity!!.runOnUiThread(Runnable {
                kidSpinnerAdapter!!.notifyDataSetChanged()
            })
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    private fun loadNumberOfInfants(){
        infantsList.clear()
        for (item in 0 until maxInfants) {
            infantsList.add(
                Hijo(item, item.toString())
            )
        }

        notifyInfantsAdapter()

        infantSpinnerAdapter = ArrayAdapter(activity!!, R.layout.spinner_item, infantsList)
        binding.numberOfInfants.adapter = infantSpinnerAdapter
        binding.numberOfInfants.onItemSelectedListener = infantSpinnerListener
    }

    private val infantSpinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            try{
                val infant = parent.selectedItem as Hijo
                noInfants = infant.desc.toInt()

                // creating the list of views of infants
                if(noInfants>0){
                    binding.labelVisitersInfants.visibility = TextView.VISIBLE
                }else{
                    binding.labelVisitersInfants.visibility = TextView.GONE
                }
                binding.namesInfants?.removeAllViews()
                for (i in 0 until noInfants){
                    binding.namesInfants?.addView(formManager.CreateInfantesList(i,activity,context,binding))
                }
            } catch (error: java.lang.Exception) {
                error.printStackTrace()
            }
        }
        override fun onNothingSelected(parent: AdapterView<*>) {
        }
    }

    private fun notifyInfantsAdapter(){
        try {
            activity!!.runOnUiThread(Runnable {
                infantSpinnerAdapter!!.notifyDataSetChanged()
            })
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    private fun selectDate(){
        /*
        * Se cambio la libreria nativa del DatePickerDialog por com.wdullaer:materialdatetimepicker para mas perzonalizacion
        * Author: Israel Canul
        *
        * */
        val c = Calendar.getInstance()
        val cyear = c.get(Calendar.YEAR)
        val cmonth = c.get(Calendar.MONTH)
        val cday = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog.newInstance(
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                var format = ""
                if (month + 1 < 10){
                    format = "0"
                }
                var format1 = ""
                if (day < 10){
                    format1 = "0"
                }
                val finalDate = format1 + day.toString() +"-" + format + (month+1).toString() + "-" + year.toString()
                binding.reservationDate.setText(finalDate)
                binding.reservationDate.setTextColor(Color.parseColor("#000000"))

                binding.requestReservation.background = ContextCompat.getDrawable(activity!!, R.drawable.button_green)
                binding.requestReservation.isEnabled = true
                fechaVisita = AppPreferences.nomalDateToFormat(finalDate)

            }, cyear, cmonth, cday);
        /*
        * Agregamos la logica para los dias no disponibles
        * Author: Israel Canul
        * [INICIO]
        * */
        val minDate = Calendar.getInstance()
        minDate.add(Calendar.DAY_OF_MONTH, 3)
        val minDateToPick = minDate.clone() as Calendar
        val maxDate = Calendar.getInstance()
        maxDate.add(Calendar.DAY_OF_MONTH, 365);
        val dis: MutableList<Calendar> = listOfNotNull<Calendar>(null).toMutableList()
        while (minDate.getTimeInMillis() < maxDate.getTimeInMillis()){
            var dayOfWeek = minDate.get(Calendar.DAY_OF_WEEK)
//            println("fechas desde firebase "+diasBL)
//            println("fechas desde firebase dia a preguntar "+dayOfWeek)
//            println("fechas desde firebase Ejemplos  Sunday"+Calendar.SUNDAY + "-Saturday" + Calendar.SATURDAY+ "-Wednesday" +Calendar.WEDNESDAY)
//            println("fechas desde firebase pregunta ? " + diasBL.indexOf(dayOfWeek.toString()))
//            if(diasBL.indexOf(dayOfWeek.toString()) >= 0){
//                println("fechas desde firebase Encontrado " + dayOfWeek)
//            }
//            if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) {
            if(diasBL.indexOf(dayOfWeek.toString()) >= 0){
                var temp:Calendar = minDate.clone() as Calendar
                dis.add(temp)
            }
            minDate.add(Calendar.DAY_OF_MONTH,1)
        }

        datePickerDialog.disabledDays = dis.toTypedArray()
        /*
        * Agregamos la logica para los dias no disponibles
        * Author: Israel Canul
        * [FINAL]
        * */
        datePickerDialog.minDate = minDateToPick
        datePickerDialog.show(getChildFragmentManager(), "datepicker")


    }

    private fun validateDate() : Boolean {
        var valid = true

        if (binding.reservationDate.text.toString().isEmpty()) {
            binding.reservationDate.error = resources.getString(R.string.date)
            valid = false
            binding.requestReservation.background = ContextCompat.getDrawable(activity!!, R.drawable.button_disabled)
        } else {
            binding.reservationDate.error = null
        }

        return valid
    }
    private fun validateInputs(): Boolean{

        var linearLayout: LinearLayout = binding.namesAdults
        nameAdults =  JSONArray()
        for (i in 0 until linearLayout.childCount){
            var layoutInputs = linearLayout.getChildAt(i) as LinearLayout
            var dataForAdult = JSONObject()
            for (j in 0 until layoutInputs.childCount){
                var inputLayout = layoutInputs.getChildAt(j) as TextInputLayout
                var input = inputLayout.editText as TextInputEditText
                if(input.text.toString().isEmpty()){
                    input.requestFocus()
                    val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm!!.toggleSoftInput(
                        InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY
                    )
                    return false
                } else{
                    //nameAdults?.put(input.text.toString())
                    var index = "nombre"
                    when (j) {
                        0 -> index = "nombre"
                        1 -> index = "paterno"
                        2 -> index = "materno"
                    }
                    dataForAdult.accumulate(index.toString(), input.text.toString())
                }
            }
            nameAdults?.put(dataForAdult)
        }
        println("complimentary: " + nameAdults)

        var linearLayoutChildren: LinearLayout = binding.namesChildren
        nameKids =  JSONArray()
        for (i in 0 until linearLayoutChildren.childCount){
            var layoutInputs = linearLayoutChildren.getChildAt(i) as LinearLayout
            var dataForKids = JSONObject()
            for (j in 0 until layoutInputs.childCount){
                var inputLayout = layoutInputs.getChildAt(j) as TextInputLayout
                var input = inputLayout.editText as TextInputEditText
                if(input.text.toString().isEmpty()){
                    input.requestFocus()
                    val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm!!.toggleSoftInput(
                        InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY
                    )
                    return false
                } else{
                    //nameAdults?.put(input.text.toString())
                    var index = "nombre"
                    when (j) {
                        0 -> index = "nombre"
                        1 -> index = "paterno"
                        2 -> index = "materno"
                    }
                    dataForKids.accumulate(index.toString(), input.text.toString())
                }
            }
            nameKids?.put(dataForKids)
        }
        println("complimentary: " + nameKids)

        var linearLayoutInfants: LinearLayout = binding.namesInfants
        nameInfants =  JSONArray()
        for (i in 0 until linearLayoutInfants.childCount){
            var layoutInputs = linearLayoutInfants.getChildAt(i) as LinearLayout
            var dataForInfantes = JSONObject()
            for (j in 0 until layoutInputs.childCount){
                var inputLayout = layoutInputs.getChildAt(j) as TextInputLayout
                var input = inputLayout.editText as TextInputEditText
                if(input.text.toString().isEmpty()){
                    input.requestFocus()
                    val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm!!.toggleSoftInput(
                        InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY
                    )
                    return false
                } else{
                    //nameAdults?.put(input.text.toString())
                    var index = "nombre"
                    when (j) {
                        0 -> index = "nombre"
                        1 -> index = "paterno"
                        2 -> index = "materno"
                    }
                    dataForInfantes.accumulate(index.toString(), input.text.toString())
                }
            }
            nameInfants?.put(dataForInfantes)
        }
        println("complimentary: " + nameInfants)

        return true
    }

    private fun validateFullName(): Boolean{
            var valid = true
            if (fullName.toString().isNullOrEmpty()) {
                valid = false
            }
            return valid
    }

    private fun sendReservationRequest(xUser: XUser){
        if (!validateDate()){
            return
        }
        // input validations
        if (!validateInputs()){
            return
        }
        if(!validateFullName()){
            val builder = AlertDialog.Builder(context!!)
            builder.setTitle(R.string.error_update_perfil).setMessage(R.string.error_on_fullname)
            builder.create().show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        val jsonObject = JSONObject()
        val jsonVisitantes = JSONObject()
        try {
            jsonVisitantes.put("adultos",nameAdults)
            jsonVisitantes.put("menores",nameKids)
            jsonVisitantes.put("infantes",nameInfants)


            jsonObject.put("titularReservacion", fullName)
            jsonObject.put("visitantes",jsonVisitantes)
//            jsonObject.put("adultos", noAdults)
//            jsonObject.put("nombresAdultos", nameAdults)
//            jsonObject.put("menores", noKids)
//            jsonObject.put("nombresMenores", nameKids)
//            jsonObject.put("infantes", noInfants)
//            jsonObject.put("nombresInfantes", nameInfants)
            jsonObject.put("idServicio", complimentaryTem!!.idServicio)
            jsonObject.put("servicio", complimentaryTem!!.servicio)
            jsonObject.put("fechaVista", fechaVisita)
            jsonObject.put("agencia", xUser.agencia)
            jsonObject.put("tarjeta", xUser.rcx)
            jsonObject.put("correo", xUser.correo)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        println("complimentary a reserva: $jsonObject")
        AndroidNetworking.post(AppPreferences.generarReserva2)
            .addJSONObjectBody(jsonObject) // posting json
            .addHeaders("Authorization", "bearer "+AppPreferences.userToken)
            .setTag("complimentary_reservation")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsString(object : StringRequestListener {
                override fun onResponse(response: String?) {
                    println("complimentary: onResponse ${response}")
                    binding.progressBar.visibility = View.GONE
                    if (response == "true") {
                        EventsTrackerFunctions.trackEvent(EventsTrackerFunctions.complimentaryBook)
                        findNavController().navigate(R.id.successFullReservationFragment)
                    } else if (response == "false") {
                        virtualCardUnAvailablePopup()
                    }
                }
                override fun onError(anError: ANError?) {
                    println("complimentary: error ${anError!!.message.toString()}")
                    binding.progressBar.visibility = View.GONE
                    snackBarMessage(anError!!.message.toString())
                }
            })

    }

    private fun virtualCardUnAvailablePopup(){
        val alertBuilder = AlertDialog.Builder(activity!!)
        val dialogView = this.layoutInflater.inflate(R.layout.popup_virtual_card_unavailable, null)
        val close_meButton = dialogView.findViewById<ImageButton>(R.id.closeMe)

        val mtext = dialogView.findViewById<TextView>(R.id.popupMessage)
        mtext.text = resources.getString(R.string.reservations_exceeded)

        alertBuilder.setView(dialogView)
        val alertDialog = alertBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCancelable(false)

        close_meButton.setOnClickListener { alertDialog.dismiss() }

        alertDialog.show()
    }

    private fun snackBarMessage(apiResponse: String){
        Snackbar.make(binding.complimentaryFragment,
            apiResponse,
            Snackbar.LENGTH_LONG)
            .show()
    }

}
