package com.davenet.notely.ui.editnote

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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
                hideKeyboard()
                saveNote()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun saveNote() {
        uiScope = CoroutineScope(Dispatchers.Default)
        if (viewModel.noteBeingModified.title.isBlank()) {
            Toast.makeText(context, "Note title cannot be blank", Toast.LENGTH_LONG).show()
            return
        }
        uiScope.launch {
            withContext(Dispatchers.Main) {
                viewModel.saveNote()
                findNavController().navigate(R.id.action_editNoteFragment_to_noteListFragment)
                Toast.makeText(context, "Changes saved", Toast.LENGTH_LONG).show()
            }
        }
    }
}