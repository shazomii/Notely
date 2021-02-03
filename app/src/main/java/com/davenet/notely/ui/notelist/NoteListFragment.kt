package com.davenet.notely.ui.notelist

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.davenet.notely.R
import com.davenet.notely.adapters.NotesAdapter
import com.davenet.notely.databinding.FragmentNoteListBinding
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.util.UIState
import com.davenet.notely.util.calculateNoOfColumns
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.jetbrains.anko.design.longSnackbar
import java.util.*

/**
 * A Fragment representing the list of user-created Notes.
 */
@AndroidEntryPoint
class NoteListFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val noteListViewModel: NoteListViewModel by viewModels()
    private lateinit var uiScope: CoroutineScope
    private var _binding: FragmentNoteListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NotesAdapter
    private var _layout: ConstraintLayout? = null
    private val layout get() = _layout!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
            .setDrawerLockMode(LOCK_MODE_UNLOCKED)
        _binding = FragmentNoteListBinding.inflate(inflater, container, false)

        adapter = NotesAdapter { list, action ->
            when (action) {
                ACTION_FAB_HIDE -> {
                    binding.fab.hide()
                }
                ACTION_FAB_SHOW -> {
                    binding.fab.show()
                }
                ACTION_SHARE -> {
                    shareNote(list.first())
                }
                ACTION_DELETE -> {
                    noteListViewModel.setNotesToDelete(list)
                    openAlertDialog()
                }
            }
        }

        observeViewModel()

        binding.apply {
            lifecycleOwner = this@NoteListFragment
            uiState = noteListViewModel.uiState
            noteList.adapter = adapter
            noteList.layoutManager =
                GridLayoutManager(context, calculateNoOfColumns(requireContext(), 180))
        }
        return binding.root
    }

    private fun observeViewModel() {
        with(noteListViewModel) {
            filteredNotes.observe(viewLifecycleOwner) { noteList ->
                noteList?.let {
                    if (noteList.isNotEmpty()) {
                        uiState.set(UIState.HAS_DATA)
                    } else {
                        uiState.set(UIState.EMPTY)
                    }
                    adapter.submitToList(noteList)
                    activity?.invalidateOptionsMenu()
                }
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_noteListFragment_to_editNoteFragment)
        }

        ArrayAdapter.createFromResource(
            activity?.baseContext!!,
            R.array.filter_array,
            R.layout.spinner_item,
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerNoteFilter.apply {
                this.adapter = adapter
                onItemSelectedListener = this@NoteListFragment
            }
        }

        uiScope = CoroutineScope(Dispatchers.Default)

        _layout = binding.noteListLayout
    }

    private fun openAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(
                resources.getQuantityString(
                    R.plurals.delete_dialog_title,
                    noteListViewModel.notesToDelete.value!!.size,
                    noteListViewModel.notesToDelete.value!!.size
                )
            )
            .setMessage(getString(R.string.undo_delete_snackbar))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteNotes()
                undoDeleteNotes()
            }
            .show()
    }

    private fun undoDeleteNotes() {
        layout.longSnackbar(
            resources.getQuantityString(
                R.plurals.undo_delete_snackbar_message,
                noteListViewModel.notesToDelete.value!!.size,
                noteListViewModel.notesToDelete.value!!.size
            ), getString(R.string.undo)
        ) {
            insertNotes()
        }
    }

    private fun insertNotes() {
        uiScope.launch {
            withContext(Dispatchers.Main) {
                with(noteListViewModel) {
                    if (notesToDelete.value!!.size == 1) {
                        insertNote()
                    } else {
                        insertNotes()
                    }
                }

            }
        }
    }

    private fun deleteNotes() {
        uiScope.launch {
            withContext(Dispatchers.Main) {
                with(noteListViewModel) {
                    if (notesToDelete.value!!.size == 1) {
                        deleteNote()
                    } else {
                        deleteNotes()
                    }
                }
                adapter.actionMode?.finish()
            }
        }
    }

    private fun shareNote(note: NoteEntry) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "${note.title}\n\n${note.text}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Via")
        startActivity(shareIntent)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        updateList(parent, pos)
    }

    private fun updateList(parent: AdapterView<*>?, pos: Int) {
        val itemArray = resources.getStringArray(R.array.filter_array)
        with(noteListViewModel) {
            when (parent?.getItemAtPosition(pos)) {
                itemArray[1] -> setFilter(1)
                itemArray[2] -> setFilter(2)
                itemArray[3] -> setFilter(3)
                else -> setFilter(4)
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        return
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ACTION_DELETE = "delete"
        const val ACTION_SHARE = "share"
        const val ACTION_FAB_SHOW = "fabShow"
        const val ACTION_FAB_HIDE = "fabHide"
    }
}