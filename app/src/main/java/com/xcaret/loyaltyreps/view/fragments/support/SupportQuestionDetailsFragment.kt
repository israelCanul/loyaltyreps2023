package com.xcaret.loyaltyreps.view.fragments.support


import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import com.xcaret.loyaltyreps.MainActivity

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.FragmentSupportQuestionDetailsBinding
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions

/**
 * A simple [Fragment] subclass.
 *
 */
class SupportQuestionDetailsFragment : Fragment() {

    lateinit var binding: FragmentSupportQuestionDetailsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_support_question_details, container, false)

        binding.supportSubject.text = arguments?.getString("msubject")
        binding.supportQuestion.text = arguments?.getString("mquestion")
        binding.supportAnswer.text = HtmlCompat.fromHtml(arguments?.getString("manswer")!!, HtmlCompat.FROM_HTML_MODE_LEGACY)

        binding.supportAnswer.movementMethod = LinkMovementMethod.getInstance()

        EventsTrackerFunctions.trackSupportQuestion(
            arguments?.getString("mquestion")!!,
            arguments?.getString("msubject")!!
        )

        val activity = activity as MainActivity?

        binding.chatWithExperts.setOnClickListener {
            activity!!.loadChat()
            EventsTrackerFunctions.trackEvent(EventsTrackerFunctions.chatOpen)
        }


        return binding.root
    }

}
