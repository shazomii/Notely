package com.davenet.notely.ui.editnote

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.davenet.notely.R
import com.davenet.notely.databinding.FragmentEditNoteBinding
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.util.hideKeyboard
import com.davenet.notely.viewmodels.EditNoteViewModel
import com.davenet.notely.viewmodels.EditNoteViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditNoteFragment : Fragment() {
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
        val selectedNote: NoteEntry? = arguments?.getParcelable("note")
        val viewModelFactory = EditNoteViewModelFactory(application, selectedNote)
        viewModel = ViewModelProvider(this, viewModelFactory).get(EditNoteViewModel::class.java)
        binding.editviewmodel = viewModel
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
            else -> super.onOptionsItemSelected(item)
        }
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

    override fun onPrepareOptionsMenu(menu: Menu) {
        viewModel.apply {
            noteBeingModified.observe(viewLifecycleOwner, {
                it?.let {
                    menu.findItem(R.id.action_save).isEnabled =
                        it.title.isNotBlank() && it.text.isNotBlank()
                }
            })
            mIsEdit.observe(viewLifecycleOwner, {
                it?.let {
                    menu.findItem(R.id.action_share).isVisible = it
                }
            })
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard(view, requireContext())
    }

    private fun saveNote() {
        CoroutineScope(Dispatchers.Default).launch {
            withContext(Dispatchers.Main) {
                viewModel.saveNote()
                findNavController().navigate(R.id.action_editNoteFragment_to_noteListFragment)
                Toast.makeText(context, "Changes saved", Toast.LENGTH_LONG).show()
            }
        }
    }
}