package com.davenet.notely.ui.editnote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.davenet.notely.R
import com.davenet.notely.databinding.FragmentOptionsListDialogListDialogBinding
import com.davenet.notely.util.formatDateOnly
import com.davenet.notely.util.formatTime
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

interface BottomSheetClickListener {
    fun onItemClick(item: String)
}

class OptionsListDialogFragment : BottomSheetDialogFragment() {
    var mListener: BottomSheetClickListener? = null
    private var _binding: FragmentOptionsListDialogListDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOptionsListDialogListDialogBinding.inflate(inflater, container, false)
        dialog?.setOnShowListener{
            val d = it as BottomSheetDialog
            d.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
    }

    private fun setupViews() {
        val args = arguments?.getLong("reminder")!!
        binding.apply {
            dateBottomSheet.text = formatDateOnly(args)
            timeBottomSheet.text = formatTime(args)

            modifyReminder.setOnClickListener {
                dismissAllowingStateLoss()
                mListener?.onItemClick(getString(R.string.modify))
            }

            deleteReminder.setOnClickListener {
                dismissAllowingStateLoss()
                mListener?.onItemClick(getString(R.string.delete))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(bundle: Bundle): OptionsListDialogFragment =
            OptionsListDialogFragment().apply {
                arguments = bundle
            }
    }
}