package com.davenet.notely.ui.notelist

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.davenet.notely.R
import com.davenet.notely.databinding.FragmentNoteListBinding
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.ui.NoteListener
import com.davenet.notely.ui.NotesAdapter
import com.davenet.notely.util.*
import com.davenet.notely.viewmodels.NoteListViewModel
import com.davenet.notely.viewmodels.NoteListViewModelFactory
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_note_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.design.longSnackbar


class NoteListFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private  lateinit var noteListViewModel: NoteListViewModel
    private lateinit var uiScope: CoroutineScope
    private lateinit var binding: FragmentNoteListBinding
    private lateinit var coordinator: CoordinatorLayout
    private lateinit var adapter: NotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        requireActivity().apply {
            drawer_layout.setDrawerLockMode(LOCK_MODE_UNLOCKED)
            toolbar.setVisible(true)
        }
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_note_list, container, false
        )

        val application = requireNotNull(this.activity).application

        val viewModelFactory = NoteListViewModelFactory(application)

        noteListViewModel =
            ViewModelProvider(this, viewModelFactory).get(NoteListViewModel::class.java)

        adapter = NotesAdapter(NoteListener { noteId ->
            noteListViewModel.onNoteClicked(noteId)
        })

        arguments?.let {
            if (it.containsKey(Constants.NOTE_ID)) {
                noteListViewModel.onNoteClicked(it.getInt(Constants.NOTE_ID))
                it.clear()
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
        noteListViewModel.apply {
            notes.observe(viewLifecycleOwner, { noteList ->
                noteList?.let {
                    if (noteList.isNotEmpty()) {
                        uiState.set(UIState.HAS_DATA)
                    } else {
                        uiState.set(UIState.EMPTY)
                    }
                    adapter.submitToList(noteList, noteFilter.get()!!)
                    activity?.invalidateOptionsMenu()
                }
            })

            navigateToNoteDetail.observe(viewLifecycleOwner, { noteId ->
                noteId?.let {
                    val bundle = Bundle()
                    bundle.putInt(Constants.NOTE_ID, noteId)
                    findNavController().navigate(
                        R.id.action_noteListFragment_to_editNoteFragment, bundle
                    )
                    onNoteDetailNavigated()
                }
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_noteListFragment_to_editNoteFragment)
        }

        ArrayAdapter.createFromResource(
            activity?.baseContext!!,
            R.array.filter_array,
            R.layout.spinner_item,
        ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerNoteFilter.adapter = adapter
                spinnerNoteFilter.onItemSelectedListener = this
            }

        uiScope = CoroutineScope(Dispatchers.Default)

        coordinator = activity?.findViewById(R.id.list_coordinator)!!
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
                val noteToErase = noteListViewModel.notes.value?.get(position)
                deleteNote(requireContext(), noteToErase!!)
                coordinator.longSnackbar(
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
                deleteAllNotes()
                undoDeleteNotes(requireContext(), noteListViewModel.notes.value!!)
            }
            .show()
    }

    private fun undoDeleteNotes(context: Context, noteList: List<NoteEntry>) {
        coordinator.longSnackbar(getString(R.string.notes_deleted), getString(R.string.undo)) {
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

    private fun deleteAllNotes() {
        uiScope.launch {
            withContext(Dispatchers.Main) {
                noteListViewModel.deleteAllNotes()
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
        val itemArray = resources.getStringArray(R.array.filter_array)
        when (parent?.getItemAtPosition(pos)) {
            itemArray[1] -> {
                noteListViewModel.noteFilter.set(NoteFilter.TODAY)
            }
            itemArray[2] -> {
                noteListViewModel.noteFilter.set(NoteFilter.UPCOMING)
            }
            itemArray[3] -> {
                noteListViewModel.noteFilter.set(NoteFilter.COMPLETED)
            } else -> noteListViewModel.noteFilter.set(NoteFilter.ALL)
        }
        adapter.submitToList(noteListViewModel.notes.value, noteListViewModel.noteFilter.get()!!)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        return
    }
}