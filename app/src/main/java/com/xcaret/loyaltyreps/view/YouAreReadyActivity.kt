package com.xcaret.loyaltyreps.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.xcaret.loyaltyreps.MainActivity
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.ActivityYouAreReadyBinding
import com.xcaret.loyaltyreps.util.AppPreferences

class YouAreReadyActivity : AppCompatActivity() {

    lateinit var binding: ActivityYouAreReadyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_you_are_ready)

        binding.gotoHome.setOnClickListener {
            launchHomeScreen()
        }
    }

    private fun launchHomeScreen(){
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
