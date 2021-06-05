package com.xcaret.loyaltyreps.view.fragments.training


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.FragmentTrainingResultFailsBinding

/**
 * A simple [Fragment] subclass.
 *
 */
class TrainingResultFailsFragment : Fragment() {

    lateinit var binding: FragmentTrainingResultFailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_training_result_fails, container, false)

        binding.welcomeTitle.text = resources.getString(R.string.quiz_result_title_error)
        binding.quizResultDescription.text = resources.getString(R.string.quiz_result_training_error_description)

        binding.gotoMain.setOnClickListener {
            findNavController().navigate(R.id.action_trainingResultFailsFragment_to_trainingFragment)
        }

        return binding.root
    }


}
