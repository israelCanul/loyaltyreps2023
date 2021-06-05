package com.xcaret.loyaltyreps.view.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.FragmentModuleNotAvailableBinding

/**
 * A simple [Fragment] subclass.
 *
 */
class ModuleNotAvailableFragment : Fragment() {

    lateinit var binding: FragmentModuleNotAvailableBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_module_not_available, container, false)

        var listOfTasksString = "<ul>"
        listOfTasksString += "<li style='list-style-type: circle;font-size: 14px;color: #832181;width: 10px;'>Estar activo en el programa</li>"
        listOfTasksString += "<li style='list-style-type: circle;font-size: 14px;color: #832181;width: 10px;'>Actualizar tus datos personales</li>"
        listOfTasksString += "<li style='list-style-type: circle;font-size: 14px;color: #832181;width: 10px;'>Aceptar los términos y condiciones</li>"
        listOfTasksString += "<li style='list-style-type: circle;font-size: 14px;color: #832181;width: 1px;'>Haber aprobado el quiz inicial</li>"
        listOfTasksString += "<ul>"

        binding.listOfTasks.text = HtmlCompat.fromHtml(listOfTasksString, HtmlCompat.FROM_HTML_MODE_LEGACY)

        return binding.root
    }


}
