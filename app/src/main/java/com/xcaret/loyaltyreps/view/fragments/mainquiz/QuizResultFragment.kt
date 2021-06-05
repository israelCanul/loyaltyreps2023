package com.xcaret.loyaltyreps.view.fragments.mainquiz


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.interfaces.StringRequestListener
import com.xcaret.loyaltyreps.MainActivity

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.database.XCaretLoyaltyDatabase
import com.xcaret.loyaltyreps.databinding.FragmentQuizResultBinding
import com.xcaret.loyaltyreps.model.XUser
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.AppPreferences.idRep
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * A simple [Fragment] subclass.
 *
 */
class QuizResultFragment : Fragment() {

    lateinit var binding: FragmentQuizResultBinding
    lateinit var xUserViewModel: XUserViewModel
    var score: Float = 0f
    var correctCount = 0f
    var questionCount = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_quiz_result, container, false)

        val mApplication = requireNotNull(this.activity).application
        //reference to the DAO
        val dataSource = XCaretLoyaltyDatabase.getInstance(mApplication).loyaltyDatabaseDAO
        //create instance
        val viewModelFactory = XUserViewModelFactory(dataSource, mApplication)

        xUserViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(XUserViewModel::class.java)
        binding.lifecycleOwner = this

        correctCount = arguments!!.getInt("correctCount").toFloat()
        questionCount = arguments!!.getInt("questionCount").toFloat()

        score = (correctCount * 100) / questionCount


        updateMainQuiz()


        return binding.root
    }

    private fun setResultResponse(){
        EventsTrackerFunctions.trackQuizCompleted(
            true,
            "Quiz inicial",
            true,
            score.toDouble()
        )

        val description = "Aprobaste el ${round(score*100)/100}% del quiz, ganando un total de:"

        binding.welcomeTitle.text = resources.getString(R.string.quiz_result_title_passed)
        binding.quizResultDescription.text = description
        val puntos = "${arguments!!.getInt("points")}"
        binding.resultPoints.text = puntos
        binding.quizResultInstructions.text = resources.getString(R.string.quiz_result_passed_next_instructions)
        binding.gotoMain.text = resources.getString(R.string.quiz_result_passed_mainview)
        binding.gotoStore.text = resources.getString(R.string.quiz_result_passed_store)

        successClicks()

    }

    private fun somethingWentWrong(title: String, details: String) {
        binding.welcomeTitle.text = title
        binding.quizResultDescription.text = details
        binding.resultPoints.text = ""
        binding.quizResultInstructions.visibility = View.GONE

        successClicks()
    }

    private fun successClicks(){
        binding.gotoMain.setOnClickListener {
            findNavController().navigate(R.id.actionXHome)
        }
        binding.gotoStore.setOnClickListener {
            findNavController().navigate(R.id.actionXShop)
        }
    }

    private fun updateMainQuiz(){
        val mprofile = activity as MainActivity?
        val jsonObject = JSONObject()
        try {
            jsonObject.put("idRep", idRep)
            jsonObject.put("cnMainQuizDone", true)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        AndroidNetworking.post(AppPreferences.updMainQuizRep)
            .addJSONObjectBody(jsonObject) // posting json
            .setPriority(Priority.MEDIUM)
            .addHeaders("Authorization", "bearer ${AppPreferences.userToken}")
            .addHeaders("Content-Type", "application/json")
            .setTag("update_mainquiz_totrue")
            .build()
            .getAsString(object : StringRequestListener {
                override fun onResponse(response: String?) {
                    if (response == "true") {
                        assignPoints()
                    } else if (response == "false") {
                        mprofile!!.requestResponse(resources.getString(R.string.profile_update_respose_error))
                    }
                }

                override fun onError(anError: ANError?) {
                    //snackBarMessage("¡Oops, algo salió mal, inténtalo más tarde!")
                    mprofile!!.requestResponse(resources.getString(R.string.profile_update_respose_error))
                    //findNavController().navigate(R.id.action_mainQuizFragment_to_welcomeMainQuizFragment)
                }
            })
    }

    private fun assignPoints(){

        val xuser2Update = XUser()
        val jsonObject = JSONObject()
        try {
            jsonObject.put("idRep", idRep)
            jsonObject.put("idMonedero", arguments!!.getInt("wallet"))
            jsonObject.put("idUsuario", AppPreferences.idUsuaro)
            jsonObject.put("puntos", arguments!!.getInt("points"))
            jsonObject.put("comentario", "Quiz Inicial")
            jsonObject.put("ip", AppPreferences.xip)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        xuser2Update.puntosParaArticulos = arguments!!.getInt("points")
        xuser2Update.cnMainQuiz = true

        AndroidNetworking.post(AppPreferences.asignacionPuntos)
            .addJSONObjectBody(jsonObject) // posting json
            .setPriority(Priority.MEDIUM)
            .addHeaders("Authorization", "bearer ${AppPreferences.userToken}")
            .addHeaders("Content-Type", "application/json")
            .setTag("update_mainquiz_asinacion_puntos")
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response!!.getJSONObject("value").getInt("error") != 0){
                        somethingWentWrong(
                            resources.getString(R.string.quiz_result_title_error),
                            response.getJSONObject("value").getString("detalle")
                        )
                    } else {
                        setResultResponse()
                        xUserViewModel.updateMainQuizPuntosArticulos(xuser2Update)
                    }
                }

                override fun onError(anError: ANError?) {
                    if (anError!!.errorDetail == "connectionError"){
                        AppPreferences.toastMessage(activity!!, "¡Oops, parece que algo salió mal en el proceso. Inténtalo más tarde!")
                    } else if (anError.errorCode == 401) {
                        val mActivity = activity as MainActivity?
                        mActivity!!.xUserLogout(activity!!, "La sesión ha caducado", "Vuelve a iniciar sesión")
                    }
                }
            })
    }

}
