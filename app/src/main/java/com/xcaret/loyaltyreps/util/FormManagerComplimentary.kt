package com.xcaret.loyaltyreps.util

import android.app.Activity
import android.content.Context
import android.widget.LinearLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.FragmentComplimentaryDetailsBinding

class FormManagerComplimentary {
    public fun CreateAdultOnList(i:Int, activity: FragmentActivity?, context: Context?, binding: FragmentComplimentaryDetailsBinding?, name:String, lastP:String, lastM:String):LinearLayout{
//contenedor padre [Inicio]
        var linearLayout = LinearLayout(context)
        val lParent = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        linearLayout.setOrientation(LinearLayout.HORIZONTAL)
        lParent.setMargins(0, 0, 0, 0)

        //contenedor padre [Final]

        //textinput para el nombre [INICIO]
        var textInputLayout = TextInputLayout(context)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.weight = 1f;
        lp.setMargins(0, 0, 0, 0)
        textInputLayout.layoutParams = lp
        //textinput para el nombre [FINAL]
        //textinput para el apellido materno [INICIO]
        var textInputLayoutLastnameMother = TextInputLayout(context)
        textInputLayoutLastnameMother.layoutParams = lp
        var inputTextM = TextInputEditText(ContextThemeWrapper(activity, R.style.CInput))
        inputTextM.setHint("Materno ("+(i+1)+")")
        inputTextM.isEnabled = true


        //textinput para el apellido materno [FINAL]
        //textinput para el apellido paterno [INICIO]
        var textInputLayoutLastnameFather = TextInputLayout(context)
        textInputLayoutLastnameFather.layoutParams = lp
        var inputTextP = TextInputEditText(ContextThemeWrapper(activity, R.style.CInput))
        inputTextP.setHint("Paterno ("+(i+1)+")")
        inputTextP.isEnabled = true
        //textinput para el apellido paterno [FINAL]



        var inputText = TextInputEditText(ContextThemeWrapper(activity, R.style.CInput))
//        inputText.setHint("Nombre (Adulto "+(i+1)+")")
        inputText.setHint("Nombre ("+(i+1)+")")
        inputText.isEnabled = true
        //for the first visiter we set the titular's full name
        //and disabled it in order to don´t be edited
        if(i == 0){
            inputText.isEnabled = false
            inputText.setText(name.toString())
            inputTextP.isEnabled = false
            inputTextP.setText(lastP)
            inputTextM.isEnabled = false
            inputTextM.setText(lastM)
        }
        textInputLayout.addView(inputText)
        textInputLayoutLastnameFather.addView(inputTextP)
        textInputLayoutLastnameMother.addView(inputTextM)
        linearLayout.addView(textInputLayout)
        linearLayout.addView(textInputLayoutLastnameFather)
        linearLayout.addView(textInputLayoutLastnameMother)
        return linearLayout;
    }
}