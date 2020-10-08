package com.davenet.notely.ui.editnote

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.davenet.notely.R
import com.davenet.notely.databinding.FragmentEditNoteBinding
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.viewmodels.EditNoteViewModel
import com.davenet.notely.viewmodels.EditNoteViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EditNoteFragment : Fragment() {
    private lateinit var binding: FragmentEditNoteBinding
    private lateinit var viewModel: EditNoteViewModel

    private lateinit var uiScope: CoroutineScope
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_edit_note, container, false
        )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val application = requireNotNull(this.activity).application
        val selectedNote: NoteEntry? = arguments?.getParcelable("note")
        val viewModelFactory = EditNoteViewModelFactory(application, selectedNote)
        viewModel = ViewModelProvider(this, viewModelFactory).get(EditNoteViewModel::class.java)
        binding.editviewmodel = viewModel
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveNote()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveNote() {
        uiScope = CoroutineScope(Dispatchers.Default)
        if (viewModel.noteBeingModified.title.isBlank()) {
            return
        }
        uiScope.launch {
            withContext(Dispatchers.Main) {
                viewModel.saveNote()
            }
        }
    }
}