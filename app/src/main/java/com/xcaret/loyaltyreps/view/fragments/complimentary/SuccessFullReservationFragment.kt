package com.xcaret.loyaltyreps.view.fragments.complimentary


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.FragmentSuccessFullReservationBinding

/**
 * A simple [Fragment] subclass.
 *
 */
class SuccessFullReservationFragment : Fragment() {

    lateinit var binding: FragmentSuccessFullReservationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_success_full_reservation, container, false)

        binding.gotoComplimentaries.setOnClickListener {
            findNavController().navigate(R.id.actionXComplimentaries)
        }

        return binding.root
    }
}
