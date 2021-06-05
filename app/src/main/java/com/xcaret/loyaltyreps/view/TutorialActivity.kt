package com.xcaret.loyaltyreps.view

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.ActivityTutorialBinding
import com.xcaret.loyaltyreps.util.AppPreferences

class TutorialActivity : AppCompatActivity() {

    lateinit var binding: ActivityTutorialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_tutorial)

        binding.mVideoTutorial.setVideoURI(Uri.parse("android.resource://com.xcaret.loyaltyreps/${R.raw.tutorial_lore}"))

        binding.mVideoTutorial.setOnCompletionListener {
            showPopup()
        }

        binding.mVideoTutorial.start()
    }

    override fun onResume() {
        super.onResume()

        binding.mVideoTutorial.start()
    }

    private fun showPopup(){
        val alertBuilder = AlertDialog.Builder(this)
        val dialogView = this.layoutInflater.inflate(R.layout.popup_tutorial_finished, null)

        val watchAgain = dialogView.findViewById<Button>(R.id.watchTutorial)
        val dontWatchAgain = dialogView.findViewById<Button>(R.id.gotoMainView)
        alertBuilder.setView(dialogView)


        val alertDialog = alertBuilder.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCancelable(false)
        alertDialog.show()

        watchAgain.setOnClickListener {
            alertDialog.dismiss()
            binding.mVideoTutorial.start()
        }

        dontWatchAgain.setOnClickListener {
            launchHomeScreen()
        }

    }

    private fun launchHomeScreen(){
        AppPreferences.tutorial_watched = true
        startActivity(Intent(this, YouAreReadyActivity::class.java))
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
