package com.davenet.notely.ui

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.davenet.notely.databinding.NoteItemBinding
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.ui.NotesAdapter.ViewHolder.Companion.from
import com.davenet.notely.util.NoteFilter
import com.davenet.notely.util.currentDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotesAdapter(private val clickListener: NoteListener) :
    ListAdapter<NoteEntry, RecyclerView.ViewHolder>(NoteDiffCallback()) {
    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun submitToList(list: List<NoteEntry>?, noteFilter: NoteFilter) {
        adapterScope.launch {
            withContext(Dispatchers.Main) {
                val items = when (noteFilter.ordinal) {
                    1 -> {
                        list?.map { it }
//                            ?.filter { noteEntry ->  noteEntry.reminder != null }
                            ?.filter { noteEntry ->
                                noteEntry.reminder != null && DateUtils.isToday(
                                    noteEntry.reminder!!
                                )
                            }
                    }
                    2 -> {
                        list?.map { it }
//                            ?.filter { noteEntry ->  noteEntry.reminder != null }
                            ?.filter { noteEntry -> noteEntry.reminder != null && noteEntry.reminder!! > currentDate().timeInMillis }
                    }
                    3 -> {
                        list?.map { it }
                            ?.filter { noteEntry -> noteEntry.reminder != null && noteEntry.reminder!! < currentDate().timeInMillis }
                    }
                    else -> list?.map { it }
                }
                submitList(items)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val noteItem = getItem(position)
                holder.bind(noteItem, clickListener)
            }
        }
    }

    class ViewHolder private constructor(private val binding: NoteItemBinding) :
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

class NoteDiffCallback : DiffUtil.ItemCallback<NoteEntry>() {
    override fun areItemsTheSame(oldItem: NoteEntry, newItem: NoteEntry): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NoteEntry, newItem: NoteEntry): Boolean {
        return oldItem == newItem
    }
}

class NoteListener(val clickListener: (noteId: Int) -> Unit) {
    fun onClick(noteId: Int) {
        clickListener(noteId)
    }
}