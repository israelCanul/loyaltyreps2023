package com.xcaret.loyaltyreps.view.fragments.training

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.xcaret.loyaltyreps.R

/**
 * A simple [Fragment] subclass.
 *
 */
class FullPicSlideFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_full_pic_slide, container, false)
    }


}
