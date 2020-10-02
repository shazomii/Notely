package com.davenet.notely.ui.editnote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.davenet.notely.R
import com.davenet.notely.database.DatabaseNote
import com.davenet.notely.database.getDatabase
import com.davenet.notely.databinding.FragmentEditNoteBinding
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
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_edit_note, container, false
        )



        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val application = requireNotNull(this.activity).application

        val dataSource = getDatabase(application).noteDao
        val selectedNote : DatabaseNote? = arguments?.getParcelable("note")
        val viewModelFactory = EditNoteViewModelFactory(dataSource, selectedNote)
        viewModel = ViewModelProvider(this, viewModelFactory).get(EditNoteViewModel::class.java)
        binding.apply {
            editviewmodel = viewModel
            fab.setOnClickListener {
                saveNote()
            }
        }
    }

    private fun saveNote() {
        uiScope = CoroutineScope(Dispatchers.Default)
        if (viewModel.noteBeingModified.title.isNullOrBlank()) {
            return
        }
        uiScope.launch {
            withContext(Dispatchers.Main) {
                viewModel.saveNote()
            }
        }
    }

}