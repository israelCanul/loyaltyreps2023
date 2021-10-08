package com.xcaret.loyaltyreps.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONException
import org.json.JSONObject
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.database.XCaretLoyaltyDatabase
import com.xcaret.loyaltyreps.databinding.ActivityLoginBinding
import com.xcaret.loyaltyreps.model.XUser
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    lateinit var xUserViewModel: XUserViewModel
    var currentUser = XUser()
    val CALL_PERMISION_CODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        val mApplication = requireNotNull(this).application
        //reference to the DAO
        val dataSource = XCaretLoyaltyDatabase.getInstance(mApplication).loyaltyDatabaseDAO
        //create instance
        val viewModelFactory = XUserViewModelFactory(dataSource, mApplication)

        xUserViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(XUserViewModel::class.java)

        Glide.with(this).load(R.drawable.logotipo_loyalty_rep).into(binding.loginLogo)

        if (AppPreferences.loggedIn){
            launchHomeScreen()
        }

        binding.mUser.addTextChangedListener(rcxInputTextListener)
        binding.mPassword.addTextChangedListener(passWordInputTextListener)

        binding.loginNotice.makeLinks(
            Pair("(998) 980 0390", View.OnClickListener {
                makePhoneCall()
            })
        )

    }

    private fun validMail() : Boolean {
        var valid = true
        if (binding.mUser.text.toString().isEmpty() ||
            binding.mUser.text.toString().length < 5 ) {
            binding.emailField.error = resources.getString(R.string.error_invalid_name)
            valid = false
        } else {
            binding.emailField.error = null
        }

        return valid
    }

    private fun validPassword() : Boolean {
        var valid = true
        if (binding.mPassword.text.toString().isEmpty() ||
            binding.mPassword.text.toString().length < 3) {
            binding.passwordField.error = resources.getString(R.string.error_incorrect_password)
            valid = false
        } else {
            binding.passwordField.error = null
        }
        return valid
    }

    private val rcxInputTextListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
        }

        override fun beforeTextChanged(s: CharSequence, start: Int,
                                       count: Int, after: Int) {
            binding.emailField.error = null
        }

        override fun onTextChanged(s: CharSequence, start: Int,
                                   before: Int, count: Int) {
            activeLoginButton()
        }
    }

    private val passWordInputTextListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
        }

        override fun beforeTextChanged(s: CharSequence, start: Int,
                                       count: Int, after: Int) {
            binding.passwordField.error = null
        }

        override fun onTextChanged(s: CharSequence, start: Int,
                                   before: Int, count: Int) {
            activeLoginButton()
        }
    }

    private fun activeLoginButton() {
        binding.loginButton.isEnabled = validMail() && validPassword()
        if (binding.loginButton.isEnabled) {
            binding.loginButton.setOnClickListener {
                userLogin(binding.mUser.text.toString(), binding.mPassword.text.toString())
            }
            binding.loginButton.background = ContextCompat.getDrawable(this, R.drawable.button_green)
        } else {
            binding.loginButton.background = ContextCompat.getDrawable(this, R.drawable.button_disabled)
        }
    }

    private fun userLogin(email: String, password: String){
        val jsonObject = JSONObject()
        try {
            jsonObject.put("username", email.toUpperCase())
            jsonObject.put("password", password)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        println("Token Xcaret " + jsonObject)
        println("Token Xcaret " + AppPreferences.XCARET_LONGIN+"authenticate/login")

        binding.progressBar.visibility = View.VISIBLE
        AndroidNetworking.post(AppPreferences.XCARET_LONGIN+"authenticate/login")
            .addJSONObjectBody(jsonObject) // posting json
            .setTag("login")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if (AppPreferences.userToken!!.isEmpty() &&
                        !AppPreferences.loggedIn){

                        var agencia:Int = response.getJSONObject("value").getInt("idAgencia")
                        if(agencia > 0){
                            FirebaseMessaging.getInstance().subscribeToTopic(agencia.toString()).addOnSuccessListener {
                                println("Firebase Token ${agencia.toString()}")
                            }
//                            FirebaseMessaging.getInstance().subscribeToTopic("DEVTEST").addOnSuccessListener {
//                                println("Firebase Token ${agencia.toString()}")
//                            }
                        }

                        AppPreferences.loggedIn = true
                        AppPreferences.userToken = response.getJSONObject("value").getString("token")

                        currentUser.idRep = response.getJSONObject("value").getInt("idRep")
                        currentUser.nombre = response.getJSONObject("value").getString("nombre")
                        currentUser.apellidoPaterno = response.getJSONObject("value").getString("apellidoPaterno")
                        currentUser.apellidoMaterno = response.getJSONObject("value").getString("apellidoMaterno")
                        currentUser.rcx = email
                        currentUser.puntosPorVentas = response.getJSONObject("value").getInt("puntosPorVentas")
                        currentUser.puntosParaArticulos = response.getJSONObject("value").getInt("puntosParaArticulos")
                        currentUser.puntosParaBoletos = response.getJSONObject("value").getInt("puntosParaBoletos")
                        currentUser.estatus = response.getJSONObject("value").getBoolean("estatus")
                        currentUser.intereses = response.getJSONObject("value").getString("intereses")
                        currentUser.idEdoCivil = response.getJSONObject("value").getInt("idEdoCivil")
                        currentUser.estadoCivil = response.getJSONObject("value").getString("estadoCivil")
                        currentUser.hijos = response.getJSONObject("value").getInt("hijos")
                        currentUser.correo = response.getJSONObject("value").getString("correo")
                        currentUser.telefono = response.getJSONObject("value").getString("telefono")
                        currentUser.fechaNacimiento = response.getJSONObject("value").getString("fechaNacimiento")
                        currentUser.idAgencia = response.getJSONObject("value").getInt("idAgencia")
                        currentUser.agencia = response.getJSONObject("value").getString("agencia")
                        currentUser.isTopRep = response.getJSONObject("value").getBoolean("isTopRep")
                        currentUser.cnPerFilCompletado = response.getJSONObject("value").getBoolean("cnPerFilCompletado")
                        currentUser.idMunicipioNacimiento = response.getJSONObject("value").getInt("idMunicipioNacimiento")
                        currentUser.municipioNacimiento = response.getJSONObject("value").getString("municipioNacimiento")
                        currentUser.idEstadoNacimiento = response.getJSONObject("value").getInt("idEstadoNacimiento")
                        currentUser.estadoNacimiento = response.getJSONObject("value").getString("estadoNacimiento")
                        currentUser.tokenFirebase = response.getJSONObject("value").getString("tokenFirebase")
                        currentUser.idEstatusArchivos = response.getJSONObject("value").getInt("idEstatusArchivos")
                        currentUser.dsEstatusArchivos = response.getJSONObject("value").getString("dsEstatusArchivos")
                        currentUser.cnTarjetaActiva = response.getJSONObject("value").getBoolean("cnTarjetaActiva")
                        currentUser.cnMainQuiz = response.getJSONObject("value").getBoolean("cnMainQuiz")
                        currentUser.cnAceptaPoliticas = response.getJSONObject("value").getBoolean("cnAceptaPoliticas")
                        currentUser.fechaAceptaPoliticas = response.getJSONObject("value").getString("fechaAceptaPoliticas")

                        if (response.getJSONObject("value").getJSONArray("quizzes").length() > 0) {
                            val mids: ArrayList<Int> = ArrayList()
                            for (item in 0 until response.getJSONObject("value").getJSONArray("quizzes").length()) {
                                val quizitem = response.getJSONObject("value").getJSONArray("quizzes").getJSONObject(item)
                                mids.add(quizitem.getInt("idQuiz"))
                            }
                            val intIds = "${mids}".replace("[", "")
                            val intIds2 = intIds.replace("]", "")
                            val finalids = intIds2.replace(" ", "")
                            currentUser.quizzes = finalids
                        } else {
                            currentUser.quizzes = ""
                        }
                        xUserViewModel.onUserLogin(currentUser)

                        EventsTrackerFunctions.trackLogin(email.toUpperCase())

                        AppPreferences.idRep = response.getJSONObject("value").getInt("idRep").toString()
                        AppPreferences.userRCX = response.getJSONObject("value").getString("rcx")
                    }

                    binding.loginError.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE

                    launchHomeScreen()
                }

                override fun onError(error: ANError) {
                    binding.loginError.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
            })

    }

    private fun launchHomeScreen(){
        val homeIntent = Intent(this, WelcomeActivity::class.java)
        startActivity(homeIntent)
        finish()
    }
    private fun launchRetrievePasswordScreen(){
        val homeIntent = Intent(this, WelcomeActivity::class.java)
        startActivity(homeIntent)
        finish()
    }


    override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startMain.flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        startActivity(startMain)
    }

    private fun makePhoneCall(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CALL_PHONE),
                CALL_PERMISION_CODE)
        }else{
            try {
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:9989800390")
                callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(callIntent)
            }catch (er:Error){
                println(er)
            }
        }
    }

    fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
        val spannableString = SpannableString(this.text)
        for (link in links) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    Selection.setSelection((view as TextView).text as Spannable, 0)
                    view.invalidate()
                    link.second.onClick(view)
                }
            }
            val startIndexOfLink = this.text.toString().indexOf(link.first)
            spannableString.setSpan(clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        this.movementMethod = LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

}
