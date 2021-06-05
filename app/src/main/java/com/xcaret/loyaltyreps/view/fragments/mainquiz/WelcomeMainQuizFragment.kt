package com.xcaret.loyaltyreps.view.fragments.mainquiz


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONObject
import com.xcaret.loyaltyreps.MainActivity

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.FragmentWelcomeMainQuizBinding
import com.xcaret.loyaltyreps.model.XQuestion
import com.xcaret.loyaltyreps.model.XQuestionChoice
import com.xcaret.loyaltyreps.model.XQuiz
import com.xcaret.loyaltyreps.util.AppPreferences
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 *
 */
class WelcomeMainQuizFragment : Fragment() {

    lateinit var binding: FragmentWelcomeMainQuizBinding
    var MAIN_QUIZ_ENDPOINT = "main_quiz/"
    lateinit var xquizQuestions: ArrayList<XQuestion>
    private var mainQuizBody: XQuiz? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_welcome_main_quiz, container, false)


        handleClicks()

        Handler().postDelayed({
            binding.lorePic.playAnimation()
        },200)

        return binding.root
    }

    private fun handleClicks(){
        binding.startMainQuiz.setOnClickListener {
            loadQuestions()
        }

        binding.gotoMainView.setOnClickListener {
            val mainIntent = Intent(activity!!, MainActivity::class.java)
            startActivity(mainIntent)
        }

        binding.startVideoTutorial.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeMainQuizFragment_to_trainingFragment)
        }
    }

    private fun loadQuestions(){
        xquizQuestions = ArrayList()
        AndroidNetworking.get("${AppPreferences.PUNK_API_URL}$MAIN_QUIZ_ENDPOINT")
            .setPriority(Priority.MEDIUM)
            .addHeaders("Authorization", "Token ${AppPreferences.PUNK_API_TOKEN}")
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    try {
                        if (response!!.getBoolean("main_quiz")){
                            for (quizquestion in 0 until response.getJSONArray("questions").length()){
                                val questionitem = response.getJSONArray("questions").getJSONObject(quizquestion)

                                val xQChoices: ArrayList<XQuestionChoice> = ArrayList()
                                if (questionitem.getJSONArray("choices").length() > 0){

                                    for (choice in 0 until questionitem.getJSONArray("choices").length()){
                                        val moption = questionitem.getJSONArray("choices").getJSONObject(choice)
                                        val quizOption = XQuestionChoice(
                                            moption.getInt("id"),
                                            moption.getString("option"),
                                            moption.getBoolean("is_correct"),
                                            moption.getInt("question")
                                        )
                                        xQChoices.add(quizOption)
                                    }
                                }

                                xquizQuestions.add(
                                    XQuestion(
                                        questionitem.getInt("id"),
                                        xQChoices,
                                        questionitem.getString("question"),
                                        questionitem.getInt("quiz")
                                    )
                                )
                            }

                            mainQuizBody = XQuiz(
                                response.getInt("id"),
                                xquizQuestions,
                                response.getInt("wallet"),
                                response.getString("name"),
                                response.getInt("points"),
                                response.getBoolean("main_quiz"),
                                0
                            )

                            val bundle = Bundle()
                            bundle.putInt("points", mainQuizBody!!.points)
                            bundle.putInt("wallet", mainQuizBody!!.wallet)
                            bundle.putInt("idQuiz", mainQuizBody!!.id)
                            bundle.putParcelableArrayList("mainquestions", xquizQuestions)
                            findNavController().navigate(R.id.action_welcomeMainQuizFragment_to_mainQuizFragment, bundle)
                        }
                    } catch (except: Exception) {
                        except.printStackTrace()
                    }
                }

                override fun onError(anError: ANError?) {
                    println("main quiz ${anError!!.errorDetail}")
                }
            })
    }

}
