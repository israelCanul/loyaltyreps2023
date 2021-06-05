package com.xcaret.loyaltyreps.view.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.apache.commons.lang3.StringUtils
import org.json.JSONObject

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.adapter.ChartAdapter
import com.xcaret.loyaltyreps.databinding.FragmentSalesBinding
import com.xcaret.loyaltyreps.model.ChartItem
import com.xcaret.loyaltyreps.util.AppPreferences
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 *
 */
class SalesFragment : Fragment() {

    lateinit var binding: FragmentSalesBinding

    private var chartAdapter: ChartAdapter? = null
    private var yearChartAdapter: ChartAdapter? = null
    private var chartItems: ArrayList<ChartItem> = ArrayList()
    private var chartItemsYear: ArrayList<ChartItem> = ArrayList()

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

        binding.reportByMonth.performClick()

        setUserSalesTermometer()

        println("userToken --- ${AppPreferences.userToken}")
        println("userToken --- ${AppPreferences.userToken}")

        return binding.root
    }

    private fun loadRecyclerView(){
        binding.chartRecyclerView.setHasFixedSize(true)
        binding.chartRecyclerView.layoutManager = LinearLayoutManager(activity!!, RecyclerView.HORIZONTAL, false)

        chartAdapter = ChartAdapter(activity!!, chartItems)
        yearChartAdapter = ChartAdapter(activity!!, chartItemsYear)


        binding.reportByMonth.setOnClickListener {
            binding.chartRecyclerView.adapter = chartAdapter
            binding.reportByYear.background = ContextCompat.getDrawable(activity!!, R.drawable.button_white_green_border)
            binding.reportByYear.setTextColor(Color.parseColor("#67a33c"))
            it.background = ContextCompat.getDrawable(activity!!, R.drawable.button_green)
            binding.reportByMonth.setTextColor(Color.WHITE)
            binding.monthNYear.text = monthYear
            binding.totPax.text = totalPax
        }

        binding.reportByYear.setOnClickListener {
            binding.chartRecyclerView.adapter = yearChartAdapter
            it.background = ContextCompat.getDrawable(activity!!, R.drawable.button_green)
            binding.reportByYear.setTextColor(Color.WHITE)
            binding.reportByMonth.background = ContextCompat.getDrawable(activity!!, R.drawable.button_white_green_border)
            binding.reportByMonth.setTextColor(Color.parseColor("#67a33c"))
            binding.monthNYear.text = currYear
            binding.totPax.text = totalYearPax
        }

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

        println("ahahahahahnoooo ${AppPreferences.idRep}")
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
                    println("salesresponse $response")
                    try {
                        for (arrItem in 0 until response.getJSONArray("value").length()){
                            val arr_item = response.getJSONArray("value").getJSONObject(arrItem).getJSONArray("reporte")
                            for (citem in 0 until arr_item.length()){
                                val chartitem = arr_item.getJSONObject(citem)
                                //val xpid: Int = chartitem.getInt("noPax")
                                val noPax = chartitem.getInt("noPax")


                                val citemId =  chartitem.getInt("idParque")
                                val mBackground = "chart_item_$citemId"
                                val mLogo = "icon_xpark_$citemId"

                                noPaxesM += noPax
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


}
