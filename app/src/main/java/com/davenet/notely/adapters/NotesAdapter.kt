package com.davenet.notely.adapters

import android.view.*
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import com.davenet.notely.R
import com.davenet.notely.databinding.NoteItemBinding
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.helper.DataBoundListAdapter
import com.davenet.notely.ui.notelist.NoteListFragment.Companion.ACTION_DELETE
import com.davenet.notely.ui.notelist.NoteListFragment.Companion.ACTION_FAB_HIDE
import com.davenet.notely.ui.notelist.NoteListFragment.Companion.ACTION_FAB_SHOW
import com.davenet.notely.ui.notelist.NoteListFragment.Companion.ACTION_SHARE
import com.davenet.notely.ui.notelist.NoteListFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotesAdapter(private val callback: ((List<NoteEntry>, action: String) -> Unit)?) :
    DataBoundListAdapter<NoteEntry, NoteItemBinding>(NoteDiffCallback()) {
    private val adapterScope = CoroutineScope(Dispatchers.Default)
    private val selectedItems = ArrayList<NoteEntry>()
    var actionMode: ActionMode? = null
    private var multiSelect = false
    private var allNotes = ArrayList<NoteEntry?>()
    private var cardList = ArrayList<ImageView>()
    private var showAddAllIcon = false

    override fun createBinding(parent: ViewGroup): NoteItemBinding {
        return DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.note_item, parent, false
        )
    }

    override fun bind(binding: NoteItemBinding, item: NoteEntry, position: Int) {
        with(binding) {
            note = item
            if (cardList.size > 0) cardList.clear()
            repeat(allNotes.size) {
                cardList.add(checkedImage)
            }
            checkedImage.isVisible = selectedItems.contains(item)

            root.setOnClickListener {
                note?.let { note ->
                    selectItem(note, checkedImage, it)
                }
            }
            root.setOnLongClickListener {
                actionMode = it.startActionMode(ActionModeCallback())
                note?.let { note ->
                    callback?.invoke(
                        listOf(),
                        ACTION_FAB_HIDE
                    )
                    selectItem(note, checkedImage, it)
                }
                return@setOnLongClickListener true
            }
        }
    }

    private fun selectItem(note: NoteEntry?, checkCircle: ImageView?, view: View?) {
        if (multiSelect) {
            with(selectedItems) {
                if (contains(note)) {
                    remove(note)
                    checkCircle?.isVisible = false
                    if (size == 0) {
                        callback?.invoke(listOf(), ACTION_FAB_SHOW)
                        checkCircle?.isVisible = false
                        actionMode?.finish()
                    }
                } else {
                    add(note!!)
                    checkCircle?.isVisible = true
                }
                if (size > 0) {
                    actionMode?.title = size.toString()
                }
                showAddAllIcon = allNotes.size != size
                actionMode?.invalidate()
            }
        } else {
            navigateToNote(note?.id, view!!)
        }
    }

    private fun navigateToNote(id: Int?, view: View) {
        val direction =
            NoteListFragmentDirections.actionNoteListFragmentToEditNoteFragment(id!!)
        view.findNavController().navigate(direction)
    }

    fun submitToList(list: List<NoteEntry>) {
        adapterScope.launch {
            withContext(Dispatchers.Main) {
                allNotes.clear()
                allNotes.addAll(list)
                submitList(list)
            }
        }
    }

    inner class ActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            multiSelect = true
            mode?.menuInflater?.inflate(R.menu.menu_list_action_bar, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menu?.findItem(R.id.action_bar_add_all)?.isVisible = showAddAllIcon
            menu?.findItem(R.id.action_bar_remove_all)?.isVisible = !showAddAllIcon
            menu?.findItem(R.id.action_bar_share)?.isVisible = selectedItems.size == 1
            return true
        }

        override fun onActionItemClicked(mode: ActionMode?, menuItem: MenuItem?): Boolean {
            when (menuItem?.itemId) {
                R.id.action_bar_share -> {
                    callback?.invoke(selectedItems, ACTION_SHARE)
                    mode?.finish()
                }
                R.id.action_bar_delete -> {
                    callback?.invoke(selectedItems, ACTION_DELETE)
                }
                R.id.action_bar_remove_all -> {
                    mode?.finish()
                }
                R.id.action_bar_add_all -> {
                    showAddAllIcon = false
                    mode?.invalidate()
                    if (allNotes.isNotEmpty()) {
                        selectedItems.clear()
                        allNotes.zip(cardList) { note, checkCircle ->
                            selectItem(note, checkCircle, null)
                        }
                        notifyDataSetChanged()
                    }
                }
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            callback?.invoke(
                listOf(),
                ACTION_FAB_SHOW
            )
            actionMode = null
            multiSelect = false
            selectedItems.clear()
            showAddAllIcon = false
            cardList.clear()
            notifyDataSetChanged()
        }

    }
}

class NoteDiffCallback : DiffUtil.ItemCallback<NoteEntry>() {
    override fun areItemsTheSame(oldItem: NoteEntry, newItem: NoteEntry): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NoteEntry, newItem: NoteEntry): Boolean {
        return oldItem == newItem
    }
}