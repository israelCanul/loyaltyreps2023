package com.xcaret.loyaltyreps.view.fragments.mainquiz


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.FragmentQuizFaildBinding
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions

/**
 * A simple [Fragment] subclass.
 *
 */
class QuizFaildFragment : Fragment() {

    lateinit var binding: FragmentQuizFaildBinding
    var score: Float = 0f
    var correctCount = 0f
    var questionCount = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_quiz_faild, container, false)

        correctCount = arguments!!.getInt("correctCount").toFloat()
        questionCount = arguments!!.getInt("questionCount").toFloat()

        score = (correctCount * 100) / questionCount

        binding.lorePicFail.setAnimation(R.raw.lore_enojada)
        binding.welcomeTitle.text = resources.getString(R.string.quiz_result_title_error)
        binding.quizResultDescription.text = resources.getString(R.string.quiz_result_description_error)
        binding.quizResultInstructions.text = resources.getString(R.string.quiz_result_error_next_instructions)

        EventsTrackerFunctions.trackQuizCompleted(
            true,
            "Quiz inicial",
            false,
            score.toDouble()
        )

        binding.gotoMain.setOnClickListener {
            findNavController().navigate(R.id.action_quizFaildFragment_to_welcomeMainQuizFragment)
        }

        return binding.root
    }


}
