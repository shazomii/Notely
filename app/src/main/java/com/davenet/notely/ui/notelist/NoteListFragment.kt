package com.davenet.notely.ui.notelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davenet.notely.R
import com.davenet.notely.database.DatabaseNote
import com.davenet.notely.database.getDatabase
import com.davenet.notely.databinding.FragmentNoteListBinding
import com.davenet.notely.ui.NoteListener
import com.davenet.notely.ui.NotesAdapter
import com.davenet.notely.viewmodels.NoteListViewModel
import com.davenet.notely.viewmodels.NoteListViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_note_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.design.longSnackbar

class NoteListFragment : Fragment() {

    private lateinit var noteListViewModel: NoteListViewModel
    private lateinit var uiScope: CoroutineScope
    private lateinit var binding: FragmentNoteListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_note_list, container, false
        )

        val application = requireNotNull(this.activity).application

        val dataSource = getDatabase(application).noteDao

        val viewModelFactory = NoteListViewModelFactory(dataSource, application)

        noteListViewModel = ViewModelProvider(this, viewModelFactory).get(NoteListViewModel::class.java)

        val adapter = NotesAdapter(NoteListener { noteId ->
            noteListViewModel.onNoteClicked(noteId)
        })

        noteListViewModel.apply {
            notes.observe(viewLifecycleOwner, {
                it?.let {
                    adapter.submitToList(it)
                }
            })

            navigateToNoteDetail.observe(viewLifecycleOwner, {note ->
                note?.let {
                    val bundle = Bundle()
                    bundle.putParcelable("note", note)
                    findNavController().navigate(
                        R.id.action_noteListFragment_to_editNoteFragment, bundle
                    )
                    noteListViewModel.onNoteDetailNavigated()
                }
            } )
        }

        binding.apply {
            lifecycleOwner = this@NoteListFragment
            noteList.adapter = adapter
            noteList.layoutManager = LinearLayoutManager(context)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_noteListFragment_to_editNoteFragment)
        }

        uiScope = CoroutineScope(Dispatchers.Default)

        val coordinator: CoordinatorLayout? = activity?.findViewById(R.id.list_coordinator)
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val noteList = noteListViewModel.notes.value
                val noteToErase = noteList!![position]
                deleteNote(noteToErase)

                coordinator?.longSnackbar("Note deleted", "Undo") {
                    insertNote(noteToErase)
                }
            }
        }).attachToRecyclerView(binding.noteList)
    }

    private fun deleteNote(note: DatabaseNote) {
        uiScope.launch {
            withContext(Dispatchers.Main) {
                noteListViewModel.deleteNote(note)
            }
        }
    }

    private fun insertNote(note: DatabaseNote) {
        uiScope.launch {
            withContext(Dispatchers.Main) {
                noteListViewModel.insertNote(note)
            }
        }
    }
}