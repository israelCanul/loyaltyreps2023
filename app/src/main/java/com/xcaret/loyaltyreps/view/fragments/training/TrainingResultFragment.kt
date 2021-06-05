package com.xcaret.loyaltyreps.view.fragments.training


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.FragmentTrainingResultBinding
/**
 * A simple [Fragment] subclass.
 *
 */
class TrainingResultFragment : Fragment() {

    lateinit var binding: FragmentTrainingResultBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_training_result, container, false)

        setResponseResult()

        return binding.root
    }

    private fun setResponseResult(){
        //Glide.with(activity!!).load(R.drawable.lore_quiz_passed).into(binding.lorePic)
        binding.lorePic.setAnimation(R.raw.lore_felicidades)
        val puntos = "${arguments!!.getInt("points")}"
        binding.welcomeTitle.text = resources.getString(R.string.quiz_result_title_passed)
        binding.resultPoints.text = puntos
        binding.quizResultDescription.text = resources.getString(R.string.quiz_result_passed_descriptions)

        binding.gotoMain.setOnClickListener {
            findNavController().navigate(R.id.action_trainingResultFragment_to_trainingFragment2)
        }
    }

}
