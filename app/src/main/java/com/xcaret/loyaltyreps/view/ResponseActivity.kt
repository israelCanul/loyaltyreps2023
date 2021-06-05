package com.xcaret.loyaltyreps.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_response.*
import com.xcaret.loyaltyreps.R

class ResponseActivity : AppCompatActivity() {

    var response_id = 100
    var response_detail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_response)

    }
}
