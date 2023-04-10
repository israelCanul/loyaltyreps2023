package com.xcaret.loyaltyreps

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.interfaces.StringRequestListener

import com.xcaret.loyaltyreps.database.XCaretLoyaltyDatabase
import com.xcaret.loyaltyreps.databinding.ActivityMainBinding
import com.xcaret.loyaltyreps.model.XUser
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.AppPreferences.idRep
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions
import com.xcaret.loyaltyreps.view.LoadPDFActivity
import com.xcaret.loyaltyreps.view.LoginActivity
import com.xcaret.loyaltyreps.view.fragments.profile.ProfileMyAccountFragment
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var xUserViewModel: XUserViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val mApplication = requireNotNull(this).application
        //reference to the DAO
        val dataSource = XCaretLoyaltyDatabase.getInstance(mApplication).loyaltyDatabaseDAO
        //create instance
        val viewModelFactory = XUserViewModelFactory(dataSource, mApplication)

        xUserViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(XUserViewModel::class.java)
        setupNavigation()
    }

    private fun setupNavigation() {
        val navController = Navigation.findNavController(this, R.id.mainNavigationFragment)
        //setupActionBarWithNavController(this, navController)
        setupActionBarWithNavController(this, navController, appBarConfiguration)
        binding.bottomNavigationView.itemIconTintList = null
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if(destination.label.toString().toLowerCase() != getString(R.string.menu_home).toLowerCase()){
                //println("firebase seleccionado: " + destination.label)
                EventsTrackerFunctions.trackClickButtonEvent(destination.label.toString())
            }
        }
    }

    override fun onSupportNavigateUp() =
        Navigation.findNavController(this, R.id.mainNavigationFragment).navigateUp()

    private val appBarConfiguration = AppBarConfiguration
        .Builder(
            R.id.actionXHome,
            R.id.actionXParks,
            R.id.actionXShop,
            R.id.shopResponseFragment2,
            R.id.quizResultFragment,
            R.id.trainingResultFragment,
            R.id.trainingResultFailsFragment,
            R.id.moduleNotAvailableFragment,
            R.id.actionXComplimentaries,
            R.id.successFullReservationFragment,
            R.id.profileFragment)
        .build()

    /*override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startMain.flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        startActivity(startMain)
    }*/

    fun loadChat(){
//        Smooch.init(application, Settings("5cda4d74c927b700107c67bd")) { response ->
//            if (response.error == null) {
//                ConversationActivity.show(this)
//            } else {
//            }
//        }
    }

    fun xUserLogout(context: Context, title: String, description: String){
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(description)

        builder.setPositiveButton("Aceptar"){_, _ ->
            xUserViewModel.onClear()
            AppPreferences.loggedIn = false
            AppPreferences.userToken = ""
            AppPreferences.idRep = ""
            AppPreferences.userRCX = ""
            val logIntent = Intent(context, LoginActivity::class.java)
            startActivity(logIntent)
        }

        if (title.isEmpty()) {
            builder.setNegativeButton("Cancelar"){dialog, _ ->
                dialog.dismiss()
            }
        }

        val dialog: AlertDialog = builder.create()

        dialog.setCancelable(false)
        dialog.show()
    }

    fun openActivityPDFile(title: String, fileUrl: String){
        val browserIntent = Intent(this, LoadPDFActivity::class.java)
        browserIntent.putExtra("file_title", title)
        browserIntent.putExtra("file_url", fileUrl)
        startActivity(browserIntent)
    }

    fun updateUserProfile(correo: String, telefono: String) {

        val jsonObject = JSONObject()
        try {
            jsonObject.put("idRep", AppPreferences.idRep)
            jsonObject.put("correo", correo)
            jsonObject.put("telefono", telefono)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        AndroidNetworking.post("${AppPreferences.XCARET_API_URL_ROOT}rep/update/")
            .addJSONObjectBody(jsonObject) // posting json
            .addHeaders("Authorization", "Bearer ${AppPreferences.userToken}")
            .setTag("update_user_account")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {

                override fun onError(anError: ANError?) {
                    //requestResponse("Error: Oops, parece que algo no salió bien, intenta más tarde")
                    requestResponse(resources.getString(R.string.profile_update_respose_error))
                }

                override fun onResponse(response: JSONObject) {
                    if (response.getJSONObject("value").getInt("error") == 0) {
                        val updateUser = XUser()
                        updateUser.correo = correo
                        updateUser.telefono = telefono
                        xUserViewModel.onUpdateMyAccount(updateUser)
                        requestResponse(resources.getString(R.string.profile_update_respose_success))
                    }
                }
            })
    }

    fun updateTermsAndConditionsStatus() {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("idRep", idRep)
            jsonObject.put("cnAceptaPoliticas", true)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        AndroidNetworking.post("${AppPreferences.XCARET_API_URL_ROOT}rep/updAceptaPoliticas/")
            .addJSONObjectBody(jsonObject) // posting json
            .addHeaders("Authorization", "Bearer "+AppPreferences.userToken)
            .setTag("update_user_account")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsString(object : StringRequestListener {
                override fun onError(anError: ANError?) {
                    //AppPreferences.toastMessage(this@MainActivity, "Error: Oops, parece que algo no salió bien, intenta más tarde")
                }
                override fun onResponse(response: String?) {
                    if (response == "true"){
                        val updateUser = XUser()
                        updateUser.cnAceptaPoliticas = true
                        xUserViewModel.updateAcceptTerms(updateUser)
                        //AppPreferences.toastMessage(this@MainActivity, "¡Felididades: Tus datos se actualizaron con éxito!")
                    }
                }
            })
    }

    fun requestResponse(description: String){
        val builder = AlertDialog.Builder(this)
        //builder.setTitle(title)
        builder.setMessage(description)
        builder.setPositiveButton("Cerrar"){dialog, _ ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }

}
