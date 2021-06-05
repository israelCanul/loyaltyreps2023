package com.xcaret.loyaltyreps.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.xcaret.loyaltyreps.MainActivity
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.ActivityWelcomeBinding
import com.xcaret.loyaltyreps.util.AppPreferences

class WelcomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityWelcomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome)

        if (AppPreferences.tutorial_watched){
            launchHomeScreen()
        }

        binding.startVideoTutorial.setOnClickListener {
            val vidIntent = Intent(this, TutorialActivity::class.java)
            startActivity(vidIntent)
        }

        binding.gotoMainView.setOnClickListener {
            launchHomeScreen()
        }

    }

    private fun launchHomeScreen(){
        AppPreferences.tutorial_watched = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startMain.flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        startActivity(startMain)
    }
}
