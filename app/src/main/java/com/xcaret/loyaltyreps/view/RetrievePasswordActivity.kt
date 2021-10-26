package com.xcaret.loyaltyreps.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.ActivityRetrieveBinding
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions.trackRecovery


class RetrievePasswordActivity: AppCompatActivity()  {
    val CALL_PERMISION_CODE = 1
    lateinit var binding: ActivityRetrieveBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_retrieve)
        binding.retrievepassbutton.setOnClickListener {
            if(binding.mUser.text.toString().isEmpty()){
                binding.textResponse.visibility = View.VISIBLE
                binding.textResponse.text = "Campo RCX/RRX obligatorio"
            }else{
                binding.progressBar.visibility = View.VISIBLE
                binding.retrievepassbutton.isEnabled = false
                retrievePassword(binding.mUser.text.toString())
            }
        }
        binding.activeRetrieve.makeLinks(
            Pair("(998) 980 0390", View.OnClickListener {
                makePhoneCall()
            })
        )
    }
    override fun onStart() {
        super.onStart()

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
    private fun retrievePassword(cardId:String){
        trackRecovery(cardId)
        AndroidNetworking.get(AppPreferences.XCARET_LONGIN+"authenticate/recoverPassword/${cardId}")
            .setTag("retrievepass")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsString(object : StringRequestListener {
                override fun onResponse(response: String) {
                    println("Retrieve: " + response)
                    val builder = AlertDialog.Builder(baseContext)
                    builder.setTitle(response)
                    builder.create()
                    binding.textResponse.visibility = View.VISIBLE
                    binding.textResponse.text = response
                    binding.progressBar.visibility = View.GONE
                    binding.retrievepassbutton.isEnabled = true
                    binding.mUser.text?.clear()
                }
                override fun onError(anError: ANError) {
                    println("Retrieve: Error " + anError.errorBody.toString())
                    binding.textResponse.visibility = View.VISIBLE
                    binding.textResponse.text = anError.errorBody.toString()
                    binding.progressBar.visibility = View.GONE
                    binding.retrievepassbutton.isEnabled = true
                }
            })
//            .getAsJSONObject(object : JSONObjectRequestListener {
//                override fun onResponse(response: JSONObject) {
//
//                }
//                override fun onError(error: ANError) {
////                    binding.loginError.visibility = View.VISIBLE
////                    binding.progressBar.visibility = View.GONE
//                }
//            })
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
}