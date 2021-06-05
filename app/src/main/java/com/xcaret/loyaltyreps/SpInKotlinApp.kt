package com.xcaret.loyaltyreps

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import io.smooch.core.Smooch
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions

class SpInKotlinApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Smooch.init(this)
        AppPreferences.init(this)
        EventsTrackerFunctions.firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }
}