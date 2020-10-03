package com.davenet.notely.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.davenet.notely.database.DatabaseNote
import com.davenet.notely.databinding.NoteItemBinding
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.ui.NotesAdapter.ViewHolder.Companion.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotesAdapter(val clickListener: NoteListener) :
    ListAdapter<DataItem, RecyclerView.ViewHolder>(NoteDiffCallback()) {
    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return from(parent)
    }

    fun submitToList(list: List<NoteEntry>?) {
        adapterScope.launch {
            val items = list?.map { DataItem.NoteItem(it) }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val noteItem = getItem(position) as DataItem.NoteItem
                holder.bind(noteItem.note, clickListener)
            }
        }
    }

    class ViewHolder private constructor(val binding: NoteItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: NoteEntry,
            clickListener: NoteListener
        ) {
            binding.apply {
                note = item
                this.clickListener = clickListener
                executePendingBindings()
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
}

class NoteDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

sealed class DataItem {
    abstract val id: Int?

    data class NoteItem(val note: NoteEntry) : DataItem() {
        override val id: Int? = note.id
    }
}

class NoteListener(val clickListener: (note: NoteEntry) -> Unit) {
    fun onClick(note: NoteEntry) {
        clickListener(note)
        Log.d("notelist", "selected note\'s id is " + note.title)
    }
}