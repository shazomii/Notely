package com.davenet.notely.ui.notelist

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.davenet.notely.R
import com.davenet.notely.adapters.NotesAdapter
import com.davenet.notely.databinding.FragmentNoteListBinding
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.util.UIState
import com.davenet.notely.util.calculateNoOfColumns
import com.davenet.notely.viewmodels.NoteListViewModel
import dagger.hilt.android.AndroidEntryPoint
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.design.longSnackbar

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

        adapter = NotesAdapter()

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
        noteListViewModel.sortedNotes.observe(viewLifecycleOwner) { noteList ->
            noteList?.let {
                if (noteList.isNotEmpty()) {
                    noteListViewModel.uiState.set(UIState.HAS_DATA)
                } else {
                    noteListViewModel.uiState.set(UIState.EMPTY)
                }
                adapter.submitToList(noteList)
                activity?.invalidateOptionsMenu()
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
        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val noteToErase = noteListViewModel.sortedNotes.value?.get(position)
                deleteNote(requireContext(), noteToErase!!)
                layout.longSnackbar(
                    getString(R.string.note_deleted),
                    getString(R.string.undo)
                ) {
                    restoreNote(requireContext(), noteToErase)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {

                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addActionIcon(R.drawable.ic_baseline_delete_24)
                    .setActionIconTint(Color.WHITE)
                    .addBackgroundColor(-2277816)
                    .create()
                    .decorate()

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }).attachToRecyclerView(binding.noteList)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear -> {
                openAlertDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val isVisible = noteListViewModel.uiState.get()?.equals(UIState.HAS_DATA)
        menu.findItem(R.id.action_clear).isVisible = isVisible!!
    }

    private fun openAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_all_notes))
            .setMessage(getString(R.string.confirm_delete_message))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteAllNotes(requireContext(), noteListViewModel.sortedNotes.value!!)
                undoDeleteNotes(requireContext(), noteListViewModel.sortedNotes.value!!)
            }
            .show()
    }

    private fun undoDeleteNotes(context: Context, noteList: List<NoteEntry>) {
        layout.longSnackbar(getString(R.string.notes_deleted), getString(R.string.undo)) {
            insertAllNotes(context, noteList)
        }
    }

    private fun insertAllNotes(context: Context, noteList: List<NoteEntry>) {
        uiScope.launch {
            withContext(Dispatchers.Main) {
                noteListViewModel.insertAllNotes(context, noteList)
            }
        }
    }

    private fun deleteAllNotes(context: Context, noteList: List<NoteEntry>) {
        uiScope.launch {
            withContext(Dispatchers.Main) {
                noteListViewModel.deleteAllNotes(context, noteList)
            }
        }
    }

    private fun deleteNote(context: Context, note: NoteEntry) {
        uiScope.launch {
            withContext(Dispatchers.Main) {
                noteListViewModel.deleteNote(context, note)
            }
        }
    }

    private fun restoreNote(context: Context, note: NoteEntry) {
        uiScope.launch {
            withContext(Dispatchers.Main) {
                noteListViewModel.restoreNote(context, note)
            }
        }
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
}