package com.davenet.notely.ui.editnote

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.davenet.notely.R
import com.davenet.notely.databinding.FragmentEditNoteBinding
import com.davenet.notely.util.Constants
import com.davenet.notely.util.ReminderState
import com.davenet.notely.util.hideKeyboard
import com.davenet.notely.viewmodels.EditNoteViewModel
import com.davenet.notely.viewmodels.EditNoteViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_edit_note.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditNoteFragment : Fragment(), BottomSheetClickListener {
    private lateinit var binding: FragmentEditNoteBinding
    private lateinit var viewModel: EditNoteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        requireActivity().drawer_layout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_edit_note, container, false
        )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val application = requireNotNull(this.activity).application
        val selectedNoteId: Int? = arguments?.getInt(Constants.NOTE_ID)
        val viewModelFactory = EditNoteViewModelFactory(application, selectedNoteId)
        viewModel = ViewModelProvider(this, viewModelFactory).get(EditNoteViewModel::class.java)
        binding.apply {
            lifecycleOwner = this@EditNoteFragment
            editviewmodel = viewModel
            reminderState = viewModel.reminderState
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackClicked()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reminderCard?.setOnClickListener {
            childFragmentManager.let {
                OptionsListDialogFragment.newInstance(Bundle()).apply {
                    show(it, tag)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_edit, menu)
        activity?.invalidateOptionsMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                hideKeyboard(view, requireContext())
                saveNote()
                true
            }
            R.id.action_share -> {
                shareNote()
                true
            }
            R.id.action_remind -> {
                pickDate()
                true
            }
            android.R.id.home -> {
                onBackClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        viewModel.apply {
            mIsEdit.observe(viewLifecycleOwner, {
                it?.let { isEdit ->
                    menu.findItem(R.id.action_share).isVisible = isEdit
                }
            })

            noteBeingModified.observe(viewLifecycleOwner, {
                it?.let {
                    val isNotBlank = it.title.isNotBlank() && it.text.isNotBlank()
                    menu.findItem(R.id.action_save).isEnabled = isNotBlank
                    menu.findItem(R.id.action_remind).isVisible =
                        isNotBlank && it.reminder == null && viewModel.mIsEdit.value!!

                    if (it.reminder != null) {
                        viewModel.reminderState.set(ReminderState.HAS_REMINDER)
                    } else {
                        viewModel.reminderState.set(ReminderState.NO_REMINDER)
                    }
                }
            })
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard(view, requireContext())
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        when (childFragment) {
            is OptionsListDialogFragment -> childFragment.mListener = this
        }
    }

    override fun onItemClick(item: String) {
        when (item) {
            "Modify" -> {
                pickDate()
            }
            "Delete" -> {
                cancelReminder()
            }
        }
    }

    private fun onBackClicked() {
        if (viewModel.isChanged) {
            openAlertDialog()
        } else {
            findNavController().navigate(R.id.action_editNoteFragment_to_noteListFragment)
        }
    }

    private fun openAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Discard changes?")
            .setMessage("Any changes you have made will be discarded. Continue anyway?")
            .setPositiveButton("Yes") { _, _ ->
                findNavController().navigate(R.id.action_editNoteFragment_to_noteListFragment)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun shareNote() {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, viewModel.noteBeingModified.value?.text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun pickDate() {
        CoroutineScope(Dispatchers.Default).launch {
            withContext(Dispatchers.Main) {
                viewModel.pickDate(
                    requireContext(),
                    viewModel.noteBeingModified.value!!,
                    textNoteReminder
                )
            }
        }
    }

    private fun saveNote() {
        CoroutineScope(Dispatchers.Default).launch {
            withContext(Dispatchers.Main) {
                viewModel.saveNote()
                if (viewModel.noteBeingModified.value!!.reminder != null) {
                    scheduleReminder()
                }
                findNavController().navigate(R.id.action_editNoteFragment_to_noteListFragment)
                Toast.makeText(context, "Changes saved", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun cancelReminder() {
        viewModel.cancelReminder(requireContext(), viewModel.noteBeingModified.value!!)
    }

    private fun scheduleReminder() {
        CoroutineScope(Dispatchers.Default).launch {
            withContext(Dispatchers.IO) {
                viewModel.scheduleReminder(requireContext(), viewModel.noteBeingModified.value!!)
            }
        }
    }
}