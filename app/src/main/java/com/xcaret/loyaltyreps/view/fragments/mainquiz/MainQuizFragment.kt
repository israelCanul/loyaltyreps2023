package com.xcaret.loyaltyreps.view.fragments.mainquiz

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
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
import com.xcaret.loyaltyreps.databinding.FragmentMainQuizBinding
import com.xcaret.loyaltyreps.model.XQuestion
import com.xcaret.loyaltyreps.model.XQuestionChoice
import com.xcaret.loyaltyreps.model.XUser
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 *
 */
class MainQuizFragment : Fragment() {

    lateinit var binding: FragmentMainQuizBinding
    lateinit var xUserViewModel: XUserViewModel

    var questionNumber: Int = 0
    var progressNumber: Int = 0
    var pickedAnswer : Boolean = false
    var score: Int = 0
    var items_selected = 0
    private lateinit var selected: Drawable
    private lateinit var unselected: Drawable

    lateinit var questions: ArrayList<XQuestion>

    private var idRep = 0
    private var points = 0
    private var wallet = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_quiz, container, false)

        questions = arguments!!.getParcelableArrayList("mainquestions")!!
        points = arguments!!.getInt("points")
        wallet = arguments!!.getInt("wallet")

        val mApplication = requireNotNull(this.activity).application
        //reference to the DAO
        val dataSource = XCaretLoyaltyDatabase.getInstance(mApplication).loyaltyDatabaseDAO
        //create instance
        val viewModelFactory = XUserViewModelFactory(dataSource, mApplication)

        xUserViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(XUserViewModel::class.java)
        binding.lifecycleOwner = this

        initializeViews()

        handleClicks()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initializeViews()
        handleClicks()
    }

    private fun initializeViews(){
        selected = ContextCompat.getDrawable(activity!!, R.drawable.toggle_on)!!
        unselected = ContextCompat.getDrawable(activity!!, R.drawable.toggle_off)!!
        binding.questionTitle.text = questions[questionNumber].question
        binding.quizProgress.isEnabled = false
        binding.quizProgress.max = questions.size
        binding.quizProgress.progress = 0

        printOptions()

    }

    private fun handleClicks(){
        binding.buttonNext.setOnClickListener {
            nextQuestion()
        }

        binding.buttonPrevious.setOnClickListener {
            prevQuestion()
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
                //pickedAnswer = true
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

    private fun prevQuestion(){
        if (questionNumber > 0){
            questionNumber --
            progressNumber --
            score --
            updateView()
        }
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
                it.putInt("points", points)
                it.putInt("wallet", wallet)
                it.putInt("correctCount", score)
                it.putInt("questionCount", questions.size)
            }
            binding.quizProgress.progress = 0

            print("quizresultsobject $bundle")

            if (score == questions.size) {
                //action_mainQuizFragment_to_quizResultFragment
                findNavController().navigate(
                    R.id.action_mainQuizFragment_to_quizResultFragment,
                    bundle
                )
            } else {
                findNavController().navigate(
                    R.id.action_mainQuizFragment_to_quizFaildFragment,
                    bundle
                )
            }
        }
    }

    private fun checkAnswer(){
        try{
            if (getCorrectItem().is_correct == pickedAnswer){
                score ++
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
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



}
