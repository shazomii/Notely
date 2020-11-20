package com.davenet.notely.ui.editnote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.davenet.notely.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_options_list_dialog_list_dialog.*

interface BottomSheetClickListener {
    fun onItemClick(item: String)
}

class OptionsListDialogFragment : BottomSheetDialogFragment() {
    var mListener: BottomSheetClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_options_list_dialog_list_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
    }

    private fun setupViews() {
        modifyReminder.setOnClickListener {
            dismissAllowingStateLoss()
            mListener?.onItemClick(getString(R.string.modify))
        }
        deleteReminder.setOnClickListener {
            dismissAllowingStateLoss()
            mListener?.onItemClick(getString(R.string.delete))
        }
    }

    companion object {
        fun newInstance(bundle: Bundle): OptionsListDialogFragment =
            OptionsListDialogFragment().apply {
                arguments = bundle
            }

    }
}