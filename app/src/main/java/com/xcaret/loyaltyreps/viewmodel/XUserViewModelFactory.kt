package com.xcaret.loyaltyreps.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.xcaret.loyaltyreps.database.XUserDatabaseDAO

@Suppress("UNCHECKED_CAST")
class XUserViewModelFactory (
    private val dataSource: XUserDatabaseDAO,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(XUserViewModel::class.java)) {
            return XUserViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}