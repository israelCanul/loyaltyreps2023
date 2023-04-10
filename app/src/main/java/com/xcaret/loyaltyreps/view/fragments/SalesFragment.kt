package com.xcaret.loyaltyreps.view.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.adapter.ChartAdapter
import com.xcaret.loyaltyreps.databinding.FragmentSalesBinding
import com.xcaret.loyaltyreps.model.ChartItem
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions.trackVentasSectionEvent
import kotlinx.android.synthetic.main.range_dates.view.*
import org.apache.commons.lang3.StringUtils
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


class SalesFragment : Fragment() {

    lateinit var binding: FragmentSalesBinding

    private var chartAdapter: ChartAdapter? = null
    private var yearChartAdapter: ChartAdapter? = null
    private var RangeChartAdapter: ChartAdapter? = null
    private var chartItems: ArrayList<ChartItem> = ArrayList()
    private var chartItemsYear: ArrayList<ChartItem> = ArrayList()
    private var chartItemsRange: ArrayList<ChartItem> = ArrayList()

    private var monthYear: String = ""
    private var totalPax: String = ""
    private var currYear: String = ""
    private var totalYearPax: String = ""

    var noPaxesM = 0
    var noPaxesY = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sales, container, false)

        loadRecyclerView()
        loadSales()
        loadSalesByYear()
        openCalendar()
        binding.reportByMonth.performClick()

        //se ejecuta el evento de firebase
        trackVentasSectionEvent("Mes")

        setUserSalesTermometer()

        println("userToken --- ${AppPreferences.userToken}")
        println("userToken --- ${AppPreferences.userToken}")

        handleInformationClicks()

        binding.reportByRange.setOnClickListener{
            binding.lottieAnimationViewForReport.visibility = View.GONE

            binding.getRangesReport.visibility = View.VISIBLE
            binding.chartRecyclerView.visibility = View.GONE

            binding.chartRecyclerView.adapter = RangeChartAdapter

            binding.reportByYear.background = ContextCompat.getDrawable(activity!!, R.drawable.button_white_green_border)
            binding.reportByYear.setTextColor(Color.parseColor("#67a33c"))
            binding.reportByMonth.background = ContextCompat.getDrawable(activity!!, R.drawable.button_white_green_border)
            binding.reportByMonth.setTextColor(Color.parseColor("#67a33c"))
            it.background = ContextCompat.getDrawable(activity!!, R.drawable.button_green)
            binding.reportByRange.setTextColor(Color.WHITE)
            // mostramos el control de total
            binding.totPax.visibility = View.GONE
            binding.monthNYear.visibility = View.GONE
            binding.monthNYear.text = ""

        }

        return binding.root
    }

    private fun loadRecyclerView(){
        binding.chartRecyclerView.setHasFixedSize(true)
        binding.chartRecyclerView.layoutManager = LinearLayoutManager(activity!!, RecyclerView.HORIZONTAL, false)

        chartAdapter = ChartAdapter(activity!!, chartItems)
        yearChartAdapter = ChartAdapter(activity!!, chartItemsYear)
        RangeChartAdapter = ChartAdapter(activity!!, chartItemsRange)// nuevo adapter para los rangos
        binding.reportByMonth.setOnClickListener {
            showAnimation(chartItems.count())
            binding.chartRecyclerView.adapter = chartAdapter
            binding.reportByYear.background = ContextCompat.getDrawable(activity!!, R.drawable.button_white_green_border)
            binding.reportByYear.setTextColor(Color.parseColor("#67a33c"))
            binding.reportByRange.background = ContextCompat.getDrawable(activity!!, R.drawable.button_white_green_border)
            binding.reportByRange.setTextColor(Color.parseColor("#67a33c"))

            binding.getRangesReport.visibility = View.GONE
            binding.chartRecyclerView.visibility = View.VISIBLE

            // mostramos el control de total
            binding.totPax.visibility = View.VISIBLE

            it.background = ContextCompat.getDrawable(activity!!, R.drawable.button_green)
            binding.reportByMonth.setTextColor(Color.WHITE)
            binding.monthNYear.visibility = View.VISIBLE
            binding.monthNYear.text = monthYear
            binding.totPax.text = totalPax
            //se ejecuta el evento de firebase
            trackVentasSectionEvent("Mes")
        }

        binding.reportByYear.setOnClickListener {
            showAnimation(chartItemsYear.count())
            binding.chartRecyclerView.adapter = yearChartAdapter
            it.background = ContextCompat.getDrawable(activity!!, R.drawable.button_green)
            binding.reportByYear.setTextColor(Color.WHITE)
            binding.reportByMonth.background = ContextCompat.getDrawable(activity!!, R.drawable.button_white_green_border)
            binding.reportByMonth.setTextColor(Color.parseColor("#67a33c"))
            binding.reportByRange.background = ContextCompat.getDrawable(activity!!, R.drawable.button_white_green_border)
            binding.reportByRange.setTextColor(Color.parseColor("#67a33c"))
            binding.getRangesReport.visibility = View.GONE
            binding.chartRecyclerView.visibility = View.VISIBLE
            binding.monthNYear.text = currYear
            binding.totPax.text = totalYearPax

            // mostramos el control de total
            binding.totPax.visibility = View.VISIBLE
            binding.monthNYear.visibility = View.VISIBLE
            binding.monthNYear.text = ""
            //se ejecuta el evento de firebase
            trackVentasSectionEvent("Año")
        }

    }
    private fun openCalendar(){
        binding.initialDate.setOnClickListener {
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
                    val finalDate = format + (month+1).toString() + "-" + format1 + day.toString() +"-" +  year.toString()
                    binding.initialDate.setText(finalDate)
                    binding.initialDate.setTextColor(Color.parseColor("#000000"))
                }
                , cyear, cmonth, cday);
                val minDate = Calendar.getInstance()
                minDate.add(Calendar.DAY_OF_MONTH, 0)
                val minDateToPick = minDate.clone() as Calendar
                datePickerDialog.maxDate = minDateToPick
                datePickerDialog.show(getChildFragmentManager(), "datepicker")
        }
        binding.finalDate.setOnClickListener {
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
                    val finalDate = format + (month+1).toString() + "-" + format1 + day.toString() +"-" +  year.toString()
                    binding.finalDate.setText(finalDate)
                    binding.finalDate.setTextColor(Color.parseColor("#000000"))
                }
                , cyear, cmonth, cday);
            val minDate = Calendar.getInstance()
            minDate.add(Calendar.DAY_OF_MONTH, 0)
            val minDateToPick = minDate.clone() as Calendar
            datePickerDialog.maxDate = minDateToPick
            datePickerDialog.show(getChildFragmentManager(), "datepicker")
        }
        binding.consultarDates.setOnClickListener{
            if(!binding.initialDate.text.isEmpty() && !binding.finalDate.text.isEmpty()){
                binding.camposRequeridosErrorOnRange.visibility = View.GONE
                loadSalesByRange(binding.initialDate.text.toString(), binding.finalDate.text.toString())
                binding.getRangesReport.visibility = View.GONE
                binding.chartRecyclerView.visibility = View.VISIBLE
            }else{
                binding.camposRequeridosErrorOnRange.visibility = View.VISIBLE
                if(binding.initialDate.text.isEmpty()){
                    binding.initialDate.performClick()
                } else if(binding.finalDate.text.isEmpty()){
                    binding.finalDate.performClick()
                }
            }
        }

    }
    @SuppressLint("SimpleDateFormat")
    public fun loadSalesByRange(initDate: String, finalDate:String){
        val fmt = SimpleDateFormat("MM-dd-yyyy")
        val fmtOut = SimpleDateFormat("d MMMM yyyy", Locale("es"))
        val dateIn = fmt.parse(initDate)
        val dateOut =  fmt.parse(finalDate)





        chartItemsRange.clear()
        binding.progressLoading.visibility = View.VISIBLE
        val jsonObject = JSONObject("""
            {
                "idRep": "${AppPreferences.idRep}",
                "fechaInicio": "${initDate}",
                "fechaFin": "${finalDate}"
            }
            """.trimIndent())


        AndroidNetworking.post("${AppPreferences.XCARET_API_URL_ROOT}Reporte/getVentasPorRangoFecha")
            .addJSONObjectBody(jsonObject) // posting json
            .setPriority(Priority.MEDIUM)
            .setContentType("application/json; charset=utf-8")
            .addHeaders("Authorization", "Bearer "+AppPreferences.userToken)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    binding.progressLoading.visibility = View.GONE
                    try {
                        println("Respuesta Reporte : ${response}")
                        noPaxesM = 0
                        var noPaxesR = 0
                        //for (arrItem in 0 until response.getJSONArray("value").length()){
                            val arr_item = response.getJSONArray("value")
                            for (citem in 0 until arr_item.length()){
                                val chartitem = arr_item.getJSONObject(citem)
                                //val xpid: Int = chartitem.getInt("noPax")
                                val noPax = chartitem.getInt("noPax")


                                val citemId =  chartitem.getInt("idParque")
                                val mBackground = "chart_item_$citemId"
                                val mLogo = "icon_xpark_$citemId"

                                noPaxesR += noPax as Int
                                if(noPax > 0){
                                    chartItemsRange.add(
                                        ChartItem(
                                            chartitem.getInt("idParque"),
                                            chartitem.getString("nombreParque"),
                                            noPax,
                                            mLogo,
                                            mBackground,
                                            noPaxesM
                                        )
                                    )
                                }
                                showAnimation(chartItemsRange.count())
                                totalPax = "Total pax: $noPaxesR"
                                binding.totPax.text = totalPax
                                binding.monthNYear.text = "${fmtOut.format(dateIn)} - ${fmtOut.format(dateOut)}"
                                binding.totPax.visibility = View.VISIBLE
                                binding.monthNYear.visibility = View.VISIBLE
                            }
                        //}
                        binding.chartRecyclerView.adapter!!.notifyDataSetChanged()
                        binding.progressLoading.visibility = View.GONE
                    } catch (except: Exception) {

                        except.printStackTrace()
                    }
                }
                override fun onError(error: ANError) {
                    binding.progressLoading.visibility = View.GONE
                    println("salesresponse $error")
                }
            })
    }


    @SuppressLint("SimpleDateFormat")
    private fun loadSales(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)

        val month_date = SimpleDateFormat("MMMM")
        c.set(Calendar.MONTH, month)
        val month_name = month_date.format(c.time)

        monthYear = StringUtils.capitalize("$month_name $year")

        chartItems.clear()
        noPaxesM = 0
        val jsonObject = JSONObject("""
            {
                "idRep": "${AppPreferences.idRep}",
                "idMes": [${month+1}]
            }
            """.trimIndent())

        AndroidNetworking.post("${AppPreferences.XCARET_API_URL_ROOT}reporte/getVentasMes/")
            .addJSONObjectBody(jsonObject) // posting json
            .setPriority(Priority.MEDIUM)
            .setContentType("application/json; charset=utf-8")
            .addHeaders("Authorization", "Bearer "+AppPreferences.userToken)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        noPaxesM = 0
                        for (arrItem in 0 until response.getJSONArray("value").length()){
                            val arr_item = response.getJSONArray("value").getJSONObject(arrItem).getJSONArray("reporte")
                            for (citem in 0 until arr_item.length()){
                                val chartitem = arr_item.getJSONObject(citem)
                                //val xpid: Int = chartitem.getInt("noPax")
                                val noPax = chartitem.getInt("noPax")

                                val citemId =  chartitem.getInt("idParque")
                                var mBackground = "chart_item_$citemId"
                                var mLogo = "icon_xpark_$citemId"
                                Log.i("Id Parque",citemId.toString())
                                when (citemId){
                                    in 1..10 -> {
                                        mLogo = "icon_xpark_$citemId"
                                        mBackground = "chart_item_$citemId"
                                    }
                                    else ->{
                                        mLogo = "lore_face_progress"
                                        mBackground = "chart_item_default"
                                    }
                                }
                                Log.i("Id Logo ", mLogo)

                                    noPaxesM += noPax
                                    if (noPax > 0) {
                                        chartItems.add(
                                            ChartItem(
                                                chartitem.getInt("idParque"),
                                                chartitem.getString("nombreParque"),
                                                noPax,
                                                mLogo,
                                                mBackground,
                                                noPaxesM
                                            )
                                        )
                                    }

                                totalPax = "Total pax: $noPaxesM"
                                binding.totPax.text = totalPax
                            }
                        }
                        binding.chartRecyclerView.adapter!!.notifyDataSetChanged()
                        binding.progressLoading.visibility = View.GONE
                    } catch (except: Exception) {
                        except.printStackTrace()
                    }
                }

                override fun onError(error: ANError) {
                    println("salesresponse $error")
                }
            })
    }

    @SuppressLint("SimpleDateFormat")
    private fun loadSalesByYear(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)

        val month_date = SimpleDateFormat("MMMM")
        c.set(Calendar.MONTH, month)
        val month_name = month_date.format(c.time)

        currYear = StringUtils.capitalize("Año $year")

        chartItemsYear.clear()
        noPaxesY = 0
        AndroidNetworking.get("${AppPreferences.XCARET_API_URL_ROOT}reporte/getVentasAnio/${AppPreferences.idRep}")
            .setPriority(Priority.MEDIUM)
            .addHeaders("Authorization", "Bearer "+AppPreferences.userToken)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    println("salesresponsepoints $response")
                    for (arrItem in 0 until response.getJSONArray("value").length()){
                        val chartitem = response.getJSONArray("value").getJSONObject(arrItem)

                        //chartitem.getInt("noPax")
                        val noPax = chartitem.getInt("noPax")
                        val citemId =  chartitem.getInt("idParque")
                        val mBackground = "chart_item_$citemId"
                        val mLogo = "icon_xpark_$citemId"


                        //val noPaxes: IntArray = intArrayOf(noPax)

                        noPaxesY += noPax
                        if(noPax > 0){
                            chartItemsYear.add(
                                ChartItem(
                                    chartitem.getInt("idParque"),
                                    chartitem.getString("nombreParque"),
                                    noPax,
                                    mLogo,
                                    mBackground,
                                    noPaxesY
                                )
                            )
                        }
                        totalYearPax = "Total pax: $noPaxesY"
                    }
                    binding.chartRecyclerView.adapter!!.notifyDataSetChanged()
                    binding.progressLoading.visibility = View.GONE
                }

                override fun onError(error: ANError) {
                    println("salesresponsepoints $error")
                }
            })
    }

    private fun setUserSalesTermometer(){
        binding.mSeekBar.isEnabled = false
        //mSeekBar.progress = 200
        AndroidNetworking.get("${AppPreferences.XCARET_API_URL_ROOT}rep/getTermometro/${AppPreferences.idRep}")
            .setPriority(Priority.MEDIUM)
            .addHeaders("Authorization", "Bearer "+AppPreferences.userToken)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    val mdias = "¡Te quedan <b><font color=\"#832181\">${response.getJSONObject("value").getString("diasFaltantes")} días</font></b> " +
                            "para convertirte en <b><font color=\"#832181\">Top Rep</font></b>!"

                    binding.daysLeft.setText(HtmlCompat.fromHtml(mdias, HtmlCompat.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)

                    val total_points = "${NumberFormat.getNumberInstance(Locale.US).format(response.getJSONObject("value").getInt("totalPuntosTopRep"))} pts"
                    binding.totalPointsTopRep.text = total_points

                    val topRepp = response.getJSONObject("value").getInt("totalPuntosTopRep")
                    binding.mSeekBar.max = topRepp
                    binding.mSeekBar.progress = response.getJSONObject("value").getInt("puntosAcumulados")
                    //val accumulated_points = "${binding.mSeekBar.progress} pts"
                    val accumulated_points = "${NumberFormat.getNumberInstance(Locale.US).format(binding.mSeekBar.progress)} pts"
                    binding.textProgress.text = accumulated_points

                    val points_left = topRepp - binding.mSeekBar.progress
                    val final_date = AppPreferences.formatStringToDate(response.getJSONObject("value").getString("fechaLimiteCorte"))
                    val dias_antes_de = "Debes acumular un total de " +
                            "<b><font color=\"#832181\"> ${NumberFormat.getNumberInstance(Locale.US).format(points_left)} pts</font></b> " +
                            "antes del <b><font color=\"#832181\"> $final_date</font></b>."

                    binding.expirationDate.setText(HtmlCompat.fromHtml(dias_antes_de, HtmlCompat.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
                }

                override fun onError(error: ANError) {
                    println("eeeeeooo$error")
                }
            })

        /*val seekBarParams = mSeekBar.layoutParams as ViewGroup.MarginLayoutParams
        val mmx = seekBarParams.leftMargin + mSeekBar.paddingLeft + mSeekBar.thumb.bounds.left - seek_text.width /2
        seek_text.x = mmx.toFloat()*/

    }

    private fun handleInformationClicks(){
        binding.howrepsleveswork.setOnClickListener {
            informationPopup(R.layout.sales_popup_rep_info)
        }
    }

    private fun informationPopup(layoutId: Int){
        val alertBuilder = AlertDialog.Builder(activity)
        val dialogView = activity!!.layoutInflater.inflate(layoutId, null)
        alertBuilder.setView(dialogView)
        val alertDialog = alertBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        dialogView.findViewById<ImageButton>(R.id.closeMe).setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.setCancelable(false)
    }
    private fun showAnimation(count: Int){
        if(count > 5)
            binding.lottieAnimationViewForReport.visibility = View.VISIBLE
        else
            binding.lottieAnimationViewForReport.visibility = View.GONE
    }

}
