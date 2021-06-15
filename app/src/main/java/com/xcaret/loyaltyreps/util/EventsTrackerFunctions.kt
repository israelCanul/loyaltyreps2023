package com.xcaret.loyaltyreps.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.xcaret.loyaltyreps.model.XProduct
import org.json.JSONObject

object EventsTrackerFunctions {
    //para los eventos
    /*
    * Israel Canul Firebase
    * */
    lateinit var firebaseAnalytics: FirebaseAnalytics

    val eventLogin = "login"
    val repCardOpen = "tarjeta_rep"
    val repCardError = "tarjeta_rep_error"
    val chatOpen = "chat"
    val operativeGuideOpen = "guia_operativa"
    val complimentaryBook = "reserva_cortesia"
    val newsFeedFeaturedView = "noticia_destacada"
    val pickupQuery = "consulta_pickups"
    val pickupSearch = "busqueda_pickups"
    val supportQuestion = "pregunta_soporte"
    val redeemArticle = "canje_articulo"
    val quizCompleted = "quiz_completado"

    fun trackLogin(rcxRrx: String){
        val params = Bundle()
        params.putString("rcx", rcxRrx)
        firebaseAnalytics.logEvent(eventLogin, params)
    }

    fun trackQuizCompleted(quizMain: Boolean, quizName: String, approved:Boolean, score:Double){
        val params = Bundle()
        params.let {
            it.putString("rcx", AppPreferences.userRCX)
            it.putBoolean("aprobado", approved)
            it.putDouble("puntuacion", score)
            it.putBoolean("main_quiz", quizMain)
            it.putString(FirebaseAnalytics.Param.ITEM_NAME, quizName)
        }
        firebaseAnalytics.logEvent(quizCompleted, params)
    }

    fun trackRedeemArticle(xProduct: XProduct){
        val params = Bundle()
        params.let {
            it.putString("rcx", AppPreferences.userRCX)
            it.putInt(FirebaseAnalytics.Param.ITEM_ID, xProduct.id_art)
            it.putInt(FirebaseAnalytics.Param.PRICE, xProduct.puntos)
            it.putInt(FirebaseAnalytics.Param.ITEM_CATEGORY, xProduct.idCategoriaArticulo)
            it.putString(FirebaseAnalytics.Param.ITEM_NAME, xProduct.nombre)
        }
        firebaseAnalytics.logEvent(redeemArticle, params)
    }

    fun trackSupportQuestion(title: String, topic: String){
        val params = Bundle()
        params.let {
            it.putString("rcx", AppPreferences.userRCX)
            it.putString("tema", topic)
            it.putString("pregunta", title)
        }
        firebaseAnalytics.logEvent(supportQuestion, params)
    }

    fun trackPickupEvent(query: String){
        val params = Bundle()
        params.putString("rcx", AppPreferences.userRCX)
        params.putString("query", query)
        firebaseAnalytics.logEvent(pickupQuery, params)
    }

    fun trackPickupSearchEvent(searchText: String){
        val params = Bundle()
        params.let {
            it.putString("rcx", AppPreferences.userRCX)
            it.putString("query", searchText)
        }
        firebaseAnalytics.logEvent(pickupSearch, params)
    }

    fun trackEvent(event: String){
        val params = Bundle()
        params.putString("rcx", AppPreferences.userRCX)
        firebaseAnalytics.logEvent(event, params)
    }
/*

func trackPickupSearchEvent(_ searchText:String,query:PickUpQuery){
    guard var parameters = query.dictionary else{return}
    parameters["rcx"] = getRepRCX()
    parameters["pickup_search_term"] = searchText
    Analytics.logEvent(KEY.AnalyticsEvent.pickupSearch, parameters:parameters)
}

* */
}