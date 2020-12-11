package com.davenet.notely.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.davenet.notely.databinding.NoteItemBinding
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.ui.notelist.NoteListFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotesAdapter : ListAdapter<NoteEntry, RecyclerView.ViewHolder>(NoteDiffCallback()) {
    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun submitToList(list: List<NoteEntry>?) {
        adapterScope.launch {
            withContext(Dispatchers.Main) {
                val items = list?.map { it }
                submitList(items)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val noteItem = getItem(position)
        (holder as ViewHolder).bind(noteItem)
    }

    class ViewHolder(private val binding: NoteItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoteEntry) {
            binding.apply {
                note = item
                setClickListener {
                    binding.note?.let { note ->
                        navigateToNote(note, it)
                    }
                }
            }
        }

        private fun navigateToNote(note: NoteEntry, view: View) {
            val direction =
                NoteListFragmentDirections.actionNoteListFragmentToEditNoteFragment(note.id!!)
            view.findNavController().navigate(direction)
        }
    }

    companion object {
        fun from(parent: ViewGroup): ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = NoteItemBinding.inflate(layoutInflater, parent, false)
            return ViewHolder(binding)
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