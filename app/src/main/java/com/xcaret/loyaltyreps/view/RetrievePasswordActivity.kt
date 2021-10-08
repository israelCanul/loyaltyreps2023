package com.xcaret.loyaltyreps.view

import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.ActivityRetrieveBinding
import com.xcaret.loyaltyreps.util.AppPreferences


class RetrievePasswordActivity: AppCompatActivity()  {

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
    }

    override fun onStart() {
        super.onStart()

    }

    private fun retrievePassword(cardId:String){

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
}