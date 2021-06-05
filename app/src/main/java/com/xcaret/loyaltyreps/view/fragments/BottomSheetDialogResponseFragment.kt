package com.xcaret.loyaltyreps.view.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottom_dialog_sheet_shop_response.*
import com.xcaret.loyaltyreps.R


class BottomSheetDialogResponseFragment : BottomSheetDialogFragment(){

    var mView: View? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            window?.setDimAmount(0f)
            setOnShowListener { setupBottomSheetDialog(this) }
        }
    }

    private fun setupBottomSheetDialog(dialog: BottomSheetDialog) {
        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout? ?: return
        BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            isHideable = true
            setBottomSheetCallback(getBottomSheetCallback())
        }
    }

    private fun getBottomSheetCallback(): BottomSheetBehavior.BottomSheetCallback? {
        return object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            @SuppressLint("SwitchIntDef")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        dismiss()
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.bottom_dialog_sheet_shop_response, container, false)

        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViews()

        val bundleArgs = arguments

        canjeTitle.text = bundleArgs!!.getString("canje_title")

        gotoStore.setOnClickListener {

            //mView!!.findNavController().navigate(R.id.action_bottomSheetDialogResponseFragment_to_actionXShop)
        }
        //setupViews()
    }

    private fun findViews() {
        //toolbar = tb_my_view
    }

    companion object {
        fun getInstance(): BottomSheetDialogResponseFragment {
            return BottomSheetDialogResponseFragment()
        }
    }

}