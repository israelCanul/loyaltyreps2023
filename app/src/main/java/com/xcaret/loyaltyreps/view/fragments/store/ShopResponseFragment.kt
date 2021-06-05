package com.xcaret.loyaltyreps.view.fragments.store

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_shop_response.*

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.database.XCaretLoyaltyDatabase
import com.xcaret.loyaltyreps.databinding.FragmentShopResponseBinding
import com.xcaret.loyaltyreps.model.XUser
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory
import java.text.NumberFormat
import java.util.*

class ShopResponseFragment : Fragment() {

    lateinit var binding: FragmentShopResponseBinding
    lateinit var xUserViewModel: XUserViewModel
    var response_id = 100
    var response_detail: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_shop_response, container, false)
        val mApplication = requireNotNull(this.activity).application
        val dataSource = XCaretLoyaltyDatabase.getInstance(mApplication).loyaltyDatabaseDAO
        val viewModelFactory = XUserViewModelFactory(dataSource, mApplication)
        xUserViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(XUserViewModel::class.java)
        binding.lifecycleOwner = this

        loadXUserData()

        return binding.root
    }

    private fun loadXUserData(){
        xUserViewModel.currentXUser.observe(this, Observer {
                xuser ->
                xuser?.let {
                    loadData(it)
                }
        })
    }

    private fun loadData(xUser: XUser){

        response_id = arguments!!.getInt("response_id")
        //response_detail = arguments!!.getString("response_details")
        if (arguments!!.getString("p_type") == "esrifa") {
            //val totalPoints = xUser.puntosParaArticulos + xUser.puntosParaBoletos
            val totalPoints = "${NumberFormat.getNumberInstance(Locale.US).format(xUser.puntosParaArticulos)} pts"
            binding.successResponse1.text = resources.getString(R.string.success_response1)
            binding.responseTotalPoints.text = totalPoints

        } else if (arguments!!.getString("p_type") == "articulo") {
            val totalPoints = "${NumberFormat.getNumberInstance(Locale.US).format(xUser.puntosParaArticulos)} pts"
            binding.successResponse1.text = resources.getString(R.string.success_response1)
            binding.responseTotalPoints.text = totalPoints
        }

        if (response_id == 0){
            response_detail = resources.getString(R.string.tu_canje_fue_exitoso)//arguments!!.getString("response_details")
            binding.lorePic.setAnimation(R.raw.lore_tarjeta)

            binding.getHelp.visibility = View.GONE
            binding.successResponse3.visibility = View.GONE

            binding.goToShop.setOnClickListener { findNavController().navigate(R.id.actionXShop) }

        } else {
            response_detail = arguments!!.getString("response_details")
            binding.canjeTitle.setTextColor(ContextCompat.getColor(activity!!, R.color.failedResponse))
            binding.lorePic.setAnimation(R.raw.lore_azul)
            //binding.successResponse1.visibility = View.GONE
            binding.successResponse2.visibility = View.GONE

            binding.goToShop.setOnClickListener { findNavController().navigate(R.id.actionXShop) }

            binding.getHelp.setOnClickListener { findNavController().navigate(R.id.action_shopResponseFragment2_to_supportFragment) }
        }

        Handler().postDelayed({
            binding.lorePic.playAnimation()
        },100)
        binding.canjeTitle.text = response_detail
    }

}
