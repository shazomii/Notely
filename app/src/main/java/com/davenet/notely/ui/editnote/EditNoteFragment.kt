package com.davenet.notely.ui.editnote

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.davenet.notely.R
import com.davenet.notely.databinding.FragmentEditNoteBinding
import com.davenet.notely.util.ReminderAvailableState
import com.davenet.notely.util.ReminderCompletionState
import com.davenet.notely.util.currentDate
import com.davenet.notely.util.hideKeyboard
import com.davenet.notely.viewmodels.EditNoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

/**
 * A Fragment representing a single Note detail screen.
 */
@AndroidEntryPoint
class EditNoteFragment : Fragment(), BottomSheetClickListener, DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var pickedDateTime: Calendar
    private lateinit var currentDateTime: Calendar
    private lateinit var uiScope: CoroutineScope
    private val args: EditNoteFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: EditNoteViewModel.AssistedFactory

    private val viewModel: EditNoteViewModel by viewModels {
        EditNoteViewModel.provideFactory(
            viewModelFactory,
            args.noteId
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        _binding = FragmentEditNoteBinding.inflate(
            inflater, container, false
        )
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
            .setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            editviewmodel = viewModel
            reminderAvailableState = viewModel.reminderAvailableState
            reminderCompletionState = viewModel.reminderCompletionState
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackClicked()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        observeViewModel()
    }

    private fun observeViewModel() {
        with(viewModel) {
            noteBeingModified.observe(viewLifecycleOwner, { note ->
                note?.let {
                    if (it.reminder != null) {
                        reminderAvailableState.set(ReminderAvailableState.HAS_REMINDER)
                        if (currentDate().timeInMillis > note.reminder!!) {
                            reminderCompletionState.set(ReminderCompletionState.COMPLETED)
                        } else {
                            reminderCompletionState.set(ReminderCompletionState.ONGOING)
                        }
                    } else {
                        reminderAvailableState.set(ReminderAvailableState.NO_REMINDER)
                    }
                    activity?.invalidateOptionsMenu()
                }
            })

        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiScope = CoroutineScope(Dispatchers.Default)

        binding.reminderCard.setOnClickListener {
            if (viewModel.reminderAvailableState.get() == ReminderAvailableState.NO_REMINDER) {
                pickDate()
            } else {
                childFragmentManager.let {
                    val bundle = Bundle().also {
                        it.putLong("reminder", viewModel.noteBeingModified.value?.reminder!!)
                    }
                    OptionsListDialogFragment.newInstance(bundle).apply {
                        show(it, tag)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_edit, menu)
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
            R.id.action_color -> {
                viewModel.pickColor(requireActivity())
                true
            }
            R.id.action_delete -> {
                openDeleteDialog()
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
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_delete).isVisible = viewModel.mIsEdit.value!!
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
            getString(R.string.modify) -> {
                pickDate()
            }
            getString(R.string.delete) -> {
                viewModel.cancelReminder()
            }
        }
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        pickedDateTime = currentDate()
        pickedDateTime.set(p1, p2, p3)
        currentDateTime = currentDate()
        val hourOfDay = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val minuteOfDay = currentDateTime.get(Calendar.MINUTE)
        val timePickerDialog =
            TimePickerDialog(requireContext(), this, hourOfDay, minuteOfDay, false)
        timePickerDialog.show()
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        pickedDateTime.set(Calendar.HOUR_OF_DAY, p1)
        pickedDateTime.set(Calendar.MINUTE, p2)
        if (pickedDateTime.timeInMillis <= currentDate().timeInMillis) {
            pickedDateTime.run {
                set(Calendar.DAY_OF_MONTH, currentDateTime.get(Calendar.DAY_OF_MONTH) + 1)
                set(Calendar.YEAR, currentDateTime.get(Calendar.YEAR))
                set(Calendar.MONTH, currentDateTime.get(Calendar.MONTH))
            }
        }
        viewModel.setDateTime(pickedDateTime.timeInMillis)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onBackClicked() {
        if (viewModel.isChanged.value!!) {
            hideKeyboard(view, requireContext())
            openAlertDialog()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun openDeleteDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_note))
            .setMessage(getString(R.string.confirm_delete_message))
            .setPositiveButton("Delete") { _, _ ->
                deleteNote()
                findNavController().popBackStack()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun deleteNote() {
        uiScope.launch {
            withContext(Dispatchers.Main) {
                viewModel.deleteNote()
            }
        }
    }

    private fun openAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.discard))
            .setMessage(getString(R.string.discard_changes))
            .setCancelable(false)
            .setPositiveButton("Continue editing", null)
            .setNegativeButton(getString(R.string.discard_note)) { _, _ ->
                findNavController().popBackStack()
            }
            .show()
    }

    private fun shareNote() {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "${viewModel.noteBeingModified.value?.title}\n\n${viewModel.noteBeingModified.value?.text}"
            )
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun saveNote() {
        with(viewModel) {
            when {
                noteBeingModified.value!!.title.isBlank() or noteBeingModified.value!!.text.isBlank() -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.not_be_blank),
                        Toast.LENGTH_LONG
                    ).show()
                }
                isChanged.value!! -> {
                    saveNote()
                    scheduleReminder()
                    Toast.makeText(context, getString(R.string.changes_saved), Toast.LENGTH_LONG)
                        .show()
                    findNavController().popBackStack()
                }
                else -> {
                    findNavController().popBackStack()

                }
            }
        }
    }

    private fun pickDate() {
        currentDateTime = currentDate()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog =
            DatePickerDialog(requireContext(), this, startYear, startMonth, startDay)
        datePickerDialog.show()
    }
}