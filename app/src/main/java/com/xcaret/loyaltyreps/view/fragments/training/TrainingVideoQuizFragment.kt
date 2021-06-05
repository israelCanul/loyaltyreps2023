package com.xcaret.loyaltyreps.view.fragments.training

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.interfaces.StringRequestListener
import com.google.android.material.snackbar.Snackbar
import com.xcaret.loyaltyreps.MainActivity
import org.json.JSONException
import org.json.JSONObject

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.database.XCaretLoyaltyDatabase
import com.xcaret.loyaltyreps.databinding.FragmentTrainingVideoQuizBinding
import com.xcaret.loyaltyreps.model.XQuestion
import com.xcaret.loyaltyreps.model.XQuestionChoice
import com.xcaret.loyaltyreps.model.XUser
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.AppPreferences.idRep
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory
import java.lang.StringBuilder

class TrainingVideoQuizFragment : Fragment(){

    lateinit var binding: FragmentTrainingVideoQuizBinding
    lateinit var xUserViewModel: XUserViewModel

    var questionNumber: Int = 0
    var progressNumber: Int = 0
    var pickedAnswer : Boolean = false
    var score: Int = 0
    var items_selected = 0
    private lateinit var selected: Drawable
    private lateinit var unselected: Drawable

    lateinit var questions: ArrayList<XQuestion>

    lateinit var choicesSelected: ArrayList<XQuestionChoice>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_training_video_quiz, container, false)

        val mApplication = requireNotNull(this.activity).application
        //reference to the DAO
        val dataSource = XCaretLoyaltyDatabase.getInstance(mApplication).loyaltyDatabaseDAO
        //create instance
        val viewModelFactory = XUserViewModelFactory(dataSource, mApplication)

        xUserViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(XUserViewModel::class.java)
        binding.lifecycleOwner = this

        questions = arguments!!.getParcelableArrayList("mainquestions")!!

        choicesSelected = ArrayList()

        return  binding.root
    }

    override fun onResume() {
        super.onResume()
        initializeViews()
        handleClicks()
    }

    private fun initializeViews(){
        selected = ContextCompat.getDrawable(activity!!, R.drawable.toggle_on)!!
        unselected = ContextCompat.getDrawable(activity!!, R.drawable.toggle_off)!!

        var videoInstructions = "Ganarás <font color=\"#8f2081\">${arguments!!.getInt("points")} puntos</font> al responder el quiz correctamente.\n"
        videoInstructions += "Lee con atención las preguntas y selecciona la respuesta correcta."

        binding.quizInstructions.text = HtmlCompat.fromHtml(videoInstructions, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.videoQuizTitle.text = arguments!!.getString("comentario")
        binding.questionTitle.text = questions[questionNumber].question
        binding.quizProgress.isEnabled = false
        binding.quizProgress.max = questions.size
        binding.quizProgress.progress = progressNumber

        printOptions()
    }

    private fun handleClicks(){
        binding.buttonNext.setOnClickListener {
            nextQuestion()
        }
    }

    private fun printOptions(){
        items_selected = 0
        binding.quizQuestionOptionsContainer.removeAllViews()
        for (choice in questions[questionNumber].choices){
            if (choice.question == questions[questionNumber].id) {
                binding.quizQuestionOptionsContainer.addView(createOptionItem(choice))
            }
        }
    }

    private fun createOptionItem(option: XQuestionChoice) : RadioButton {
        val mOption = RadioButton(activity)
        mOption.id = option.id
        mOption.text = option.option
        mOption.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        mOption.compoundDrawablePadding = 15
        mOption.isChecked = false
        mOption.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                pickedAnswer = option.is_correct!!
                items_selected +=1
                binding.quizError.visibility = View.GONE
            } else {
                if (items_selected > 0) { items_selected -=1 }
                if (items_selected == 0) {
                    binding.quizError.visibility = View.VISIBLE
                }
            }
        }
        mOption.setBackgroundColor(Color.TRANSPARENT)

        return mOption

    }


    private fun updateView(){
        binding.questionTitle.text = questions[questionNumber].question
        binding.quizProgress.progress = progressNumber
        printOptions()
    }

    private fun nextQuestion(){
        checkAnswer()
        if (questionNumber < questions.size-1) {
            if (items_selected > 0) {
                questionNumber ++
                progressNumber ++
                updateView()
            } else {
                binding.quizError.visibility = View.VISIBLE
            }
            if (questionNumber == questions.size -1) {
                binding.buttonNext.text = resources.getString(R.string.quiz_finish)
            }
        } else if (questionNumber < questions.size){
            val bundle = Bundle().also {
                it.putInt("points", arguments!!.getInt("points"))
            }

            binding.quizProgress.progress = 0
            if (score == questions.size) {
                EventsTrackerFunctions.trackQuizCompleted(
                    false,
                    arguments!!.getString("comentario")!!,
                    true,
                    score.toDouble()
                )
                addUserQuiz(bundle)
            } else {
                EventsTrackerFunctions.trackQuizCompleted(
                    false,
                    arguments!!.getString("comentario")!!,
                    false,
                    score.toDouble()
                )
                findNavController().navigate(
                    R.id.action_trainingVideoQuizFragment_to_trainingResultFailsFragment
                )
            }
        }
    }

    private fun checkAnswer(){
        if (getCorrectItem().is_correct == pickedAnswer){
            score ++
        }
        println("my current score $score")
    }

    private fun getCorrectItem() : XQuestionChoice {
        var correctAnswer: XQuestionChoice? = null
        for (item in questions[questionNumber].choices){
            if (item.is_correct!!){
                correctAnswer = item
            }
        }
        return correctAnswer!!
    }

    private fun addUserQuiz(bundle: Bundle){
        val mprofile = activity as MainActivity?
        val jsonObject = JSONObject()
        try {
            jsonObject.put("idRep", idRep)
            jsonObject.put("idQuiz", arguments!!.getInt("idQuiz"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        AndroidNetworking.post(AppPreferences.addRepQuiz)
            .addJSONObjectBody(jsonObject) // posting json
            .setPriority(Priority.MEDIUM)
            .addHeaders("Authorization", "bearer ${AppPreferences.userToken}")
            .addHeaders("Content-Type", "application/json")
            .setTag("add_user_quiz")
            .build()
            .getAsString(object : StringRequestListener {
                override fun onResponse(response: String?) {
                    if (response == "true") {
                        assignPoints()
                        findNavController().navigate(
                            R.id.action_trainingVideoQuizFragment_to_trainingResultFragment,
                            bundle
                        )
                    }
                }

                override fun onError(anError: ANError?) {
                    mprofile!!.requestResponse(resources.getString(R.string.profile_update_respose_error))
                    //findNavController().navigate(R.id.to_trainingVideoQuizFragment)
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
            jsonObject.put("comentario", "Quiz - ${arguments!!.getString("comentario")}")
            jsonObject.put("ip", AppPreferences.xip)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        if (arguments!!.getInt("wallet") == 1) {
            xuser2Update.puntosParaArticulos = arguments!!.getInt("points")
            xuser2Update.puntosParaBoletos = 0
        } else if (arguments!!.getInt("wallet") == 2) {
            xuser2Update.puntosParaBoletos = arguments!!.getInt("points")
            xuser2Update.puntosParaArticulos = 0
        }

        val separation = if (!AppPreferences.quizzesIds.isEmpty()) "," else ""
        val newString = StringBuilder(AppPreferences.quizzesIds).append("$separation${arguments!!.getInt("idQuiz")}")

        xuser2Update.quizzes = newString.toString()

        AndroidNetworking.post(AppPreferences.asignacionPuntos)
            .addJSONObjectBody(jsonObject) // posting json
            .setPriority(Priority.MEDIUM)
            .addHeaders("Authorization", "bearer ${AppPreferences.userToken}")
            .addHeaders("Content-Type", "application/json")
            .setTag("update_mainquiz_asinacion_puntos")
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response!!.getJSONObject("value").getInt("error") == 0){
                        xUserViewModel.updateUserAssignPoints(xuser2Update)
                        //snackBarMessage(response.getJSONObject("value").getString("detalle"))
                    }
                }

                override fun onError(anError: ANError?) {

                }
            })
    }

    private fun snackBarMessage(apiResponse: String){
        Snackbar.make(binding.mainContainer,
            apiResponse,
            Snackbar.LENGTH_LONG)
            .show()
    }

}
