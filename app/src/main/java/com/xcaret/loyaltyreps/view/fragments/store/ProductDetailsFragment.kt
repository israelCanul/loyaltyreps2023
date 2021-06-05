package com.xcaret.loyaltyreps.view.fragments.store

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.xcaret.loyaltyreps.MainActivity
import org.json.JSONObject

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.database.XCaretLoyaltyDatabase
import com.xcaret.loyaltyreps.databinding.FragmentProductDetailsBinding
import com.xcaret.loyaltyreps.model.XProduct
import com.xcaret.loyaltyreps.model.XUser
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class ProductDetailsFragment : Fragment() {

    lateinit var binding: FragmentProductDetailsBinding
    lateinit var xUserViewModel: XUserViewModel

    lateinit var countDownTimer: CountDownTimer

    val END_POINT = "Articulo/canjear"
    var productItem: XProduct? = null
    var qty = 1

    var dialogView: View? = null
    var alertDialog: AlertDialog? = null
    var totalDePutos = 0
    var currentTotal = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_product_details, container, false)
        val mApplication = requireNotNull(this.activity).application
        val dataSource = XCaretLoyaltyDatabase.getInstance(mApplication).loyaltyDatabaseDAO
        val viewModelFactory = XUserViewModelFactory(dataSource, mApplication)
        xUserViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(XUserViewModel::class.java)
        binding.lifecycleOwner = this

        productItem = arguments!!.getParcelable("xitem") as XProduct

        loadProductDetails()

        xUserViewModel.currentXUser.observe(this, Observer {
                xuser ->
                xuser?.let {
                    totalDePutos = it.puntosParaArticulos
                    lessMoreTickets(it.puntosParaArticulos)
                    canjearButtonClicked()
                }
            }
        )

        return binding.root
    }

    private fun loadProductDetails(){

        if (productItem!!.esRifa){
            binding.lessMore.visibility = View.VISIBLE
        }

        if (productItem!!.cnHotSale) {
            binding.timerContainer.visibility = View.VISIBLE
            binding.availabilityDate.text = AppPreferences.formatDate(productItem!!.feVigencia!!)

            productCountDownTimer(getTimeInMilliseconds(productItem!!.feVigencia!!))
        }

        Glide.with(activity!!).load(productItem!!.foto).into(binding.productCover)
        binding.productTitle.text = productItem!!.nombre
        val itemPrice = "${productItem!!.puntos} pts"
        binding.productPoints.text = itemPrice
        binding.productDescription.text = productItem!!.descripcion

        binding.productQty.text = qty.toString()
        binding.totalToSpend.text = totalPoints(qty * productItem!!.puntos)
        currentTotal = qty * productItem!!.puntos
    }

    private fun productCountDownTimer(milliseconds: Long) {

        println("milliseconds given $milliseconds")
        countDownTimer = object : CountDownTimer(milliseconds, 1000) {
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                var millisUntilFinished = millisUntilFinished

                val secondsInMilli: Long = 1000

                val minutesInMilli = secondsInMilli * 60

                val hoursInMilli = minutesInMilli * 60

                val elapsedDays = millisUntilFinished / (24 * 60 * 60 * secondsInMilli)
                millisUntilFinished -= elapsedDays * (24  *60 * 60  *1000)

                val elapsedHours = millisUntilFinished / hoursInMilli
                millisUntilFinished = millisUntilFinished % hoursInMilli

                val elapsedMinutes = millisUntilFinished / minutesInMilli
                millisUntilFinished = millisUntilFinished % minutesInMilli

                val elapsedSeconds = millisUntilFinished / secondsInMilli

                binding.timerText.text = String.format("%02d:%02d:%02d:%02d", elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds)
            }

            override fun onFinish() {
                binding.timerText.text = resources.getString(R.string.hote_sale_ended)
                //binding.canjear.background = ContextCompat.getDrawable(activity!!, R.drawable.button_disabled)
                //binding.canjear.isEnabled = false
            }
        }

        countDownTimer.start()
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTimeInMilliseconds(finalDate: String) : Long {
        var timeLeftInMillis = 0L

        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val fDate: Date = dateFormat.parse(finalDate)!!

            println("current time in sdsdsdmillis ${fDate.time}")

            val currDateInMillis = Date().time
            println("current time in millis $currDateInMillis")

            timeLeftInMillis = fDate.time - currDateInMillis
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        return timeLeftInMillis
    }


    private fun lessMoreTickets(puntosArticulos: Int){
        binding.addLess.setOnClickListener {
            if (qty > 1) {
                qty -= 1
            }
            binding.productQty.text = qty.toString()
            binding.totalToSpend.text = totalPoints(qty * productItem!!.puntos)
            binding.addMore.isEnabled = true
        }
        binding.addMore.setOnClickListener {
            qty += 1
            binding.productQty.text = qty.toString()
            binding.totalToSpend.text = totalPoints(qty * productItem!!.puntos)
            currentTotal = qty * productItem!!.puntos

            it.isEnabled = qty < totalTicketsPosible2Buy(totalDePutos,productItem!!.puntos)
        }
    }

    private fun totalTicketsPosible2Buy(totalPoints : Int, productPointsCost : Int) : Int {
        return totalPoints/productPointsCost
    }

    private fun totalPoints(price: Int) : String{
        return "Total: $price puntos"
    }

    private fun canjearButtonClicked(){
        binding.canjear.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(activity!!)
            val inflater = activity!!.layoutInflater
            dialogView = inflater.inflate(R.layout.alert_dialog_canjear_layout, null)

            alertDialogBuilder.setView(dialogView)

            alertDialog = alertDialogBuilder.create()

            alertDialog!!.show()


            alertDialog!!.setCanceledOnTouchOutside(false)

            dialogView!!.findViewById<Button>(R.id.shop_continue).setOnClickListener {
                dialogView!!.findViewById<ProgressBar>(R.id.mprogressBar).visibility = View.VISIBLE
                canjearProducto()

            }
            dialogView!!.findViewById<Button>(R.id.cancel_action).setOnClickListener { alertDialog!!.dismiss() }

        }
    }

    private fun canjearProducto(){

        val xuser2Update = XUser()
        val jsonObject = JSONObject("""{"articulos" : [{"idrep": "${AppPreferences.idRep}", "idarticulo": "${productItem!!.id_art}", "numBoletosSolicitados": "$qty", "ip": "${AppPreferences.xip}"}]}""")

        xuser2Update.puntosParaArticulos = productItem!!.puntos * qty
        /*if (productItem!!.esRifa) {
            xuser2Update.puntosParaBoletos = productItem!!.puntos * qty
        } else {
            xuser2Update.puntosParaArticulos = productItem!!.puntos * qty
        }*/

        AndroidNetworking.post(AppPreferences.XCARET_API_URL_ROOT+END_POINT)
            .addJSONObjectBody(jsonObject)
            .setContentType("application/json; charset=utf-8")
            .addHeaders("Authorization", "BEARER "+AppPreferences.userToken)
            .setTag("canjear")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    dialogView!!.findViewById<ProgressBar>(R.id.mprogressBar).visibility = View.GONE
                    alertDialog!!.dismiss()
                    val response_value = response.getJSONObject("value")
                    val bundle = Bundle().also {
                        it.putInt("response_id", response_value.getInt("error"))
                        it.putString("response_details", response_value.getString("detalle"))
                        if (productItem!!.esRifa){
                            it.putString("p_type", "esrifa")
                        } else {
                            it.putString("p_type", "articulo")
                        }
                    }
                    println("boletosrifaresponse $response")
                    if (response_value.getInt("error") != 0) {
                        findNavController().navigate(R.id.action_productDetailsFragment_to_shopResponseFragment2, bundle)
                    } else {
                        xUserViewModel.updateXUserPointFromStore(xuser2Update)
                        EventsTrackerFunctions.trackRedeemArticle(
                            productItem!!
                        )
                        findNavController().navigate(R.id.action_productDetailsFragment_to_shopResponseFragment2, bundle)
                    }
                }

                override fun onError(error: ANError) {
                    dialogView!!.findViewById<ProgressBar>(R.id.mprogressBar).visibility = View.GONE
                    println("eeeeeooo -----sdsdsdds${error.errorBody}")
                }
            })

    }

}
