package com.xcaret.loyaltyreps.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.ActivityRetrieveBinding


class RetrievePasswordActivity: AppCompatActivity()  {
    lateinit var binding: ActivityRetrieveBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_retrieve)
    }
}