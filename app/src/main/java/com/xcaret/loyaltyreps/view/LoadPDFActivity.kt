package com.xcaret.loyaltyreps.view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.ActivityLoadPdfBinding

class LoadPDFActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoadPdfBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_load_pdf)

        val pdflink = intent.extras!!.getString("file_url")
        supportActionBar!!.title = resources.getString(R.string.go_back)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.pdfContainer.settings.javaScriptEnabled = true
        binding.pdfContainer.settings.allowFileAccess = true
        binding.pdfContainer.loadUrl("https://docs.google.com/gview?embedded=true&url=$pdflink")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}


