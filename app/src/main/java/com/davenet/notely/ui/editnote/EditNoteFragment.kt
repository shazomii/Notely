package com.davenet.notely.ui.editnote

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.DatePicker
import android.widget.TimePicker
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
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.util.*
import com.davenet.notely.viewmodels.EditNoteViewModel
import com.davenet.notely.viewmodels.EditNoteViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_edit_note.*
import kotlinx.coroutines.*
import java.util.*

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class EditNoteFragment : Fragment(), BottomSheetClickListener, DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    private lateinit var binding: FragmentEditNoteBinding
    private lateinit var viewModel: EditNoteViewModel
    private lateinit var pickedDateTime: Calendar
    private lateinit var currentDateTime: Calendar
    private lateinit var uiScope: CoroutineScope

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
            reminderCompletion = viewModel.reminderCompletion
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
        viewModel.apply {
            noteBeingModified.observe(viewLifecycleOwner, { note ->
                note?.let {
                    if (it.reminder != null) {
                        viewModel.reminderState.set(ReminderState.HAS_REMINDER)
                        if (currentDate().timeInMillis > note.reminder!!) {
                            viewModel.reminderCompletion.set(ReminderCompletion.COMPLETED)
                        } else {
                            viewModel.reminderCompletion.set(ReminderCompletion.ONGOING)
                        }
                    } else {
                        viewModel.reminderState.set(ReminderState.NO_REMINDER)
                    }
                    activity?.invalidateOptionsMenu()
                }
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiScope = CoroutineScope(Dispatchers.Default)

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
                hideKeyboard(view, requireContext())
                pickDate()
                true
            }
            R.id.action_color -> {
                pickColor(requireActivity(), viewModel.noteBeingModified.value!!)
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

    private fun openDeleteDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete note")
            .setMessage("This action cannot be undone. Sure to delete?")
            .setPositiveButton("Delete") { _, _ ->
                val note = viewModel.noteBeingModified.value!!
                deleteNote(requireContext(), note)
                findNavController().popBackStack()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteNote(context: Context, note: NoteEntry) {
        uiScope.launch {
            withContext(Dispatchers.Main) {
                viewModel.deleteNote(context, note)
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val isVisible = viewModel.reminderState.get()?.equals(ReminderState.NO_REMINDER)
        menu.findItem(R.id.action_remind).isVisible = isVisible!!
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
                cancelReminder()
            }
        }
    }

    private fun onBackClicked() {
        if (viewModel.isChanged.value!!) {
            openAlertDialog()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun openAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.discard))
            .setMessage(getString(R.string.discard_changes))
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val note = viewModel.noteBeingModified.value!!
                if (note.title.isNotEmpty() and note.text.isNotEmpty()) {
                    saveNote()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.not_be_blank),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton(getString(R.string.discard_note)) { _, _ ->
                findNavController().popBackStack()
            }
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

    private fun pickColor(activity: Activity, note: NoteEntry) {
        viewModel.pickColor(activity, note)
    }

    private fun saveNote() {
        when {
            viewModel.noteBeingModified.value!!.title.isBlank() or viewModel.noteBeingModified.value!!.text.isBlank() -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.not_be_blank),
                    Toast.LENGTH_LONG
                ).show()
            }
            viewModel.isChanged.value!! -> {
                viewModel.saveNote()
                scheduleReminder()
                Toast.makeText(context, getString(R.string.changes_saved), Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
            else -> {
                findNavController().popBackStack()

            }
        }
    }

    private fun cancelReminder() {
        viewModel.cancelReminder(requireContext(), viewModel.noteBeingModified.value!!)
    }

    private fun scheduleReminder() {
        viewModel.scheduleReminder(requireContext(), viewModel.noteBeingModified.value!!)
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
}