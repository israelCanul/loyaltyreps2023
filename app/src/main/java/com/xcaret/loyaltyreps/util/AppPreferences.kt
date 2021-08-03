package com.xcaret.loyaltyreps.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.xcaret.loyaltyreps.model.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object AppPreferences {
    private const val NAME = "SpinKotlin"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences
    //lateinit var xUser: XUser

    //punk API
    /*var PUNK_API_URL = "https://app.loyaltyreps.com/api/v1/"
    val PUNK_API_TOKEN = "ab24d47537ac191b37b7617a98687cb5d5d42d5a"*/
    //punkdeve
    var PUNK_API_URL = "https://xcaret.punklabs.ninja/api/v1/"
    val PUNK_API_TOKEN = "ab24d47537ac191b37b7617a98687cb5d5d42d5a"

    //Xcaret Loyalty API production
    /*var XCARET_API_URL = "https://api.loyaltyreps.com/api/Sivex/"
    var XCARET_API_URL_ROOT = "https://api.loyaltyreps.com/api/"
    var XCARET_LONGIN = "https://api.loyaltyreps.com/"*/

    //Xcaret Dev
    var XCARET_API_URL = "https://apidev.loyaltyreps.com/api/Sivex/"
    var XCARET_API_URL_ROOT = "https://apidev.loyaltyreps.com/api/"
    var XCARET_LONGIN = "https://apidev.loyaltyreps.com/"

    //UseruploadFiles
    var virtualCardIDRul = "${XCARET_API_URL_ROOT}Files/getTarjetaImage/"

    //User History of points
    var getTopTenAsignacionPuntos = "${XCARET_API_URL_ROOT}reporte/getTopTenAsignacionPuntos"
    var getOperacionesCanje = "${XCARET_API_URL_ROOT}reporte/getOperacionesCanje"

    //main quiz urls
    var updMainQuizRep = "${XCARET_API_URL_ROOT}rep/updMainQuizRep"
    var asignacionPuntos = "${XCARET_API_URL_ROOT}rep/asignacionPuntos"
    var addRepQuiz = "${XCARET_API_URL_ROOT}rep/addRepQuiz"
    var quizzesIds: String = ""

    //Operative Guide url
    var operativeGuideUrl = "${PUNK_API_URL}documents/1/"
    var avisoDePrivacidadUrl = "${PUNK_API_URL}documents/2/"
    var terminosYCondicionesUrl = "${PUNK_API_URL}documents/3/"

    //complimentary reservation
    var generarReserva = "${XCARET_API_URL_ROOT}Reserva/generarReserva"

    private val IS_FIRST_RUN_PREF = Pair("is_first_run", true)
    private val LOGGEDIN = Pair("logged_in", false)
    private val TUTORIAL_WATCHED = Pair("tutorial_watched", false)
    private val MAIN_QUIZ_PASSED = Pair("main_quiz_passed", false)
    private val XUSER_STATUS = Pair("xuser_status", false)

    private val USER_IDAGENTE = Pair("user_idRep", "")
    private val USER_NAME = Pair("user_name", "")
    private val USER_RCX = Pair("user_rcx", "")
    private val USER_TOKEN = Pair("user_token", "")
    private val USER_STATUS_ARCHIVO = Pair("status_archivo", "")

    var xTourList: ArrayList<XTour> = ArrayList()
    var xZoneList: ArrayList<XZone> = ArrayList()
    var xParksList: ArrayList<XPark> = ArrayList()

    val selectedInterestsIds: ArrayList<Int> = ArrayList()
    var hijosList: ArrayList<Hijo> = ArrayList()
    var stadoCivilList: ArrayList<XEstadoCivil> = ArrayList()
    var estadosList: ArrayList<XEstado> = ArrayList()
    var municipiosList: ArrayList<Municipio> = ArrayList()

    var userTotalPoints = 0
    var mainquizScoreRequired = 80.0f
    var idUsuaro = 123
    var xip = "192.168.1.5"

    val userMaxLevel = 10
    val userMaxPoints = 18000

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    /**
     * SharedPreferences extension function, so we won't need to call edit() and apply()
     * ourselves on every SharedPreferences operation.
     */
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var firstRun: Boolean
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getBoolean(IS_FIRST_RUN_PREF.first, IS_FIRST_RUN_PREF.second)
        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putBoolean(IS_FIRST_RUN_PREF.first, value)
        }

    var loggedIn: Boolean
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getBoolean(LOGGEDIN.first, LOGGEDIN.second)
        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putBoolean(LOGGEDIN.first, value)
        }

    var tutorial_watched: Boolean
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getBoolean(TUTORIAL_WATCHED.first, TUTORIAL_WATCHED.second)
        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putBoolean(TUTORIAL_WATCHED.first, value)
        }

    var main_quiz_passed: Boolean
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getBoolean(MAIN_QUIZ_PASSED.first, MAIN_QUIZ_PASSED.second)
        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putBoolean(MAIN_QUIZ_PASSED.first, value)
        }

    var xuser_status: Boolean
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getBoolean(XUSER_STATUS.first, XUSER_STATUS.second)
        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putBoolean(XUSER_STATUS.first, value)
        }

    var userToken: String?
        get() = preferences.getString(USER_TOKEN.first, USER_TOKEN.second)
        set(value) = preferences.edit{
            it.putString(USER_TOKEN.first, value)
        }

    var user_status_archivo: String?
        get() = preferences.getString(USER_STATUS_ARCHIVO.first, USER_STATUS_ARCHIVO.second)
        set(value) = preferences.edit{
            it.putString(USER_STATUS_ARCHIVO.first, value)
        }

    //idRep
    var idRep: String?
    get() = preferences.getString(USER_IDAGENTE.first, USER_IDAGENTE.second)
    set(value) = preferences.edit{
        it.putString(USER_IDAGENTE.first, value)
    }

    var userRCX: String?
        get() = preferences.getString(USER_RCX.first, USER_RCX.second)
        set(value) = preferences.edit{
            it.putString(USER_RCX.first, value)
        }


    @SuppressLint("SimpleDateFormat")
    fun formatStringToDate(dt: String): String {
        try {
            //val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val convertedDate: Date?
            val formattedDate: String?

            convertedDate = sdf.parse(dt)
            formattedDate = SimpleDateFormat("EEE, dd MMM yyyy").format(convertedDate!!)

            return formattedDate
        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }

    }

    @SuppressLint("SimpleDateFormat")
    fun formatDate(dt: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val convertedDate: Date?
            val formattedDate: String?

            convertedDate = sdf.parse(dt)
            formattedDate = SimpleDateFormat("dd MMM yyyy").format(convertedDate!!)

            formattedDate
        } catch (e: ParseException) {
            e.printStackTrace()
            ""
        }

    }

    @SuppressLint("SimpleDateFormat")
    fun formatStringToDate2(dt: String): String {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            //val sdf = SimpleDateFormat("yyyy-MM-dd")
            val convertedDate: Date?
            val formattedDate: String?

            convertedDate = sdf.parse(dt)
            formattedDate = SimpleDateFormat("EEE, dd MMM yyyy").format(convertedDate!!)

            return formattedDate
        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }

    }

    @SuppressLint("SimpleDateFormat")
    fun nomalDateToFormat(mDate: String) : String {
        try {
            val sdf = SimpleDateFormat("dd-MM-yyyy")
            //val sdf = SimpleDateFormat("yyyy-MM-dd")
            val convertedDate: Date?
            val formattedDate: String?

            convertedDate = sdf.parse(mDate)
            formattedDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(convertedDate!!)

            return formattedDate
        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }
    }

    fun toastMessage(context: Context, sectionMessage: String){
        Toast.makeText(context, sectionMessage, Toast.LENGTH_SHORT).show()
    }

    fun emptyString(stringItem: String) : String {
        val newString = ""
        if (stringItem == "" || stringItem.isEmpty()){
            return newString
        }
        return stringItem
    }

}