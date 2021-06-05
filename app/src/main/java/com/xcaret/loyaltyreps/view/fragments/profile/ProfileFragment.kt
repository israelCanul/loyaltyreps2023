package com.xcaret.loyaltyreps.view.fragments.profile

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior.setTag
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.BitmapRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.interfaces.StringRequestListener
import com.bumptech.glide.Glide
import com.xcaret.loyaltyreps.MainActivity
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.adapter.MViewPagerAdapter
import com.xcaret.loyaltyreps.database.XCaretLoyaltyDatabase
import com.xcaret.loyaltyreps.databinding.FragmentProfileBinding
import com.xcaret.loyaltyreps.model.XUser
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.AppPreferences.idRep
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory
import org.json.JSONException
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 *
 */
class ProfileFragment : Fragment() {

    lateinit var binding: FragmentProfileBinding
    lateinit var mChildFragManager: FragmentManager
    lateinit var xUserViewModel: XUserViewModel

    var userTarjeta = ""
    var userEstatus = false
    var usertarjetaActiva = false
    var userCnTarjetaActiva = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile,
            container, false)
        mChildFragManager = childFragmentManager

        val mApplication = requireNotNull(this.activity).application
        //reference to the DAO
        val dataSource = XCaretLoyaltyDatabase.getInstance(mApplication).loyaltyDatabaseDAO
        //create instance
        val viewModelFactory = XUserViewModelFactory(dataSource, mApplication)
        xUserViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(XUserViewModel::class.java)
        binding.lifecycleOwner = this

        loadUserData()

        setHasOptionsMenu(true)

        binding.profileViewPager.offscreenPageLimit = 3

        setupViewPager(binding.profileViewPager)
        binding.profileTabPager.setupWithViewPager(binding.profileViewPager)

        binding.profileViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                val inputMethodManager = activity!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view!!.windowToken, 0)
            }
            override fun onPageSelected(position: Int) {
            }
        })

        return binding.root
    }

    private fun setupViewPager(viewPager: ViewPager?) {
        val shopAdapter = MViewPagerAdapter(mChildFragManager)

        shopAdapter.addFragment(ProfileMyAccountFragment(), resources.getString(R.string.section_profile_myaccount))
        shopAdapter.addFragment(ProfileInterestsFragment(), resources.getString(R.string.section_profile_interests))
        shopAdapter.addFragment(ProfileRecordFragment(), resources.getString(R.string.section_profile_record))

        viewPager!!.adapter = shopAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.user_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.viewMyCard -> {
                if (userEstatus && userCnTarjetaActiva == 3 && usertarjetaActiva){
                    showPopup()
                    EventsTrackerFunctions.trackEvent(EventsTrackerFunctions.repCardOpen)
                } else {
                    virtualCardUnAvailablePopup()
                    EventsTrackerFunctions.trackEvent(EventsTrackerFunctions.repCardError)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun loadUserData(){
        xUserViewModel.currentXUser.observe(this, Observer {
                xuser ->
                xuser?.let {
                    userTarjeta = it.rcx
                    userEstatus = it.estatus
                    usertarjetaActiva= it.cnTarjetaActiva
                    userCnTarjetaActiva = it.idEstatusArchivos

                    //println("tarjetaaaaaa $usertarjetaActiva")
                    loadUserDataFromServer()
                    /*if (it.idEstatusArchivos != 3 || !it.cnTarjetaActiva) {
                        loadUserDataFromServer()
                    }*/
                }
            }
        )
    }

    private fun showPopup(){
        val alertBuilder = AlertDialog.Builder(activity!!)
        val dialogView = this.layoutInflater.inflate(R.layout.popup_virtual_id_card, null)

        val xuserCard = dialogView.findViewById<ImageView>(R.id.xuserVirtualId)
        val close_meButton = dialogView.findViewById<ImageButton>(R.id.closeMe)
        val loadingProgress = dialogView.findViewById<ProgressBar>(R.id.mprogressBar)

        alertBuilder.setView(dialogView)
        val alertDialog = alertBuilder.create()

        AndroidNetworking.get("${AppPreferences.virtualCardIDRul}${AppPreferences.userRCX}")
            .addHeaders("Authorization", "Bearer ${AppPreferences.userToken}")
            .setTag("request_user_virtual_idcard")
            .setPriority(Priority.HIGH)
            .build()
            .getAsBitmap(object : BitmapRequestListener {
                override fun onError(anError: ANError?) {
                    if (anError!!.errorCode == 401 || anError.errorCode == 400) {
                        val mActivity = activity as MainActivity?
                        mActivity!!.xUserLogout(activity!!, "La sesión ha caducado", "Vuelve a iniciar sesión")
                    }
                    alertDialog.dismiss()
                    //loadingProgress.visibility = View.GONE
                    //AppPreferences.toastMessage(activity!!, "¡Algo salió mal, inténtalo más tarde!")
                }

                override fun onResponse(response: Bitmap?) {
                    Glide.with(activity!!).load(response).into(xuserCard)
                    loadingProgress.visibility = View.GONE
                }
            })

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCancelable(false)

        close_meButton.setOnClickListener { alertDialog.dismiss() }

        alertDialog.show()

    }

    private fun virtualCardUnAvailablePopup(){
        val alertBuilder = AlertDialog.Builder(activity!!)
        val dialogView = this.layoutInflater.inflate(R.layout.popup_virtual_card_unavailable, null)
        val close_meButton = dialogView.findViewById<ImageButton>(R.id.closeMe)
        val mtext = dialogView.findViewById<TextView>(R.id.popupMessage)
        mtext.text = resources.getString(R.string.how_to_get_card)

        alertBuilder.setView(dialogView)
        val alertDialog = alertBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCancelable(false)

        close_meButton.setOnClickListener { alertDialog.dismiss() }

        alertDialog.show()
    }

    private fun loadUserDataFromServer() {
        val user2update = XUser()
        AndroidNetworking.get("${AppPreferences.XCARET_API_URL_ROOT}rep/getDetalleRepById/${AppPreferences.idRep}")
            .setTag("user_info")
            .addHeaders("Authorization", "Bearer ${AppPreferences.userToken}")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onError(anError: ANError?) {
                }

                override fun onResponse(response: JSONObject) {
                    if (response.length() > 0){
                        user2update.idEstatusArchivos = response.getJSONObject("value").getInt("idEstatusArchivos")
                        user2update.cnTarjetaActiva = response.getJSONObject("value").getBoolean("cnTarjetaActiva")
                        user2update.dsEstatusArchivos = response.getJSONObject("value").getString("dsEstatusArchivos")
                        xUserViewModel.updateStatusArchivos(user2update)
                    }
                }

            })
    }

}
