package com.xcaret.loyaltyreps.view.fragments.training


import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.FragmentTrainingParkTdetailsBinding
import com.xcaret.loyaltyreps.model.XTraining
import kotlinx.android.synthetic.main.fragment_item_list_dialog_item.view.*

class TrainingParkTDetailsFragment : Fragment() {

    lateinit var binding: FragmentTrainingParkTdetailsBinding
    var mcontent: String? = ""
    var xTraining: XTraining? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_training_park_tdetails, container, false)


        xTraining = arguments!!.getParcelable("xtraining_item")

        mcontent = "<!DOCTYPE html>"
        mcontent += "<head></head>"
        mcontent += "<body style=\"background-color:#f6eff6; font-size:13px;\"><div align=\"justify\">"
        mcontent += xTraining!!.description
        mcontent += "</div></body></html>"

        println("current html content $mcontent")
        println("current park desc ${xTraining!!.description}")

        binding.xtParkName.text = xTraining!!.name
        binding.xtParkDescription.setBackgroundColor(Color.TRANSPARENT)
        binding.xtParkDescription.loadDataWithBaseURL(null, mcontent!!, "text/html", "UTF-8", null)

        return binding.root
    }

}
