package com.davenet.notely.viewmodels

import android.content.Context
import android.text.format.DateUtils
import androidx.databinding.ObservableField
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.repository.NoteRepository
import com.davenet.notely.util.UIState
import com.davenet.notely.util.currentDate
import com.davenet.notely.work.cancelAlarm
import com.davenet.notely.work.createSchedule
import kotlinx.coroutines.launch

class NoteListViewModel @ViewModelInject internal constructor(
    private val context: Context,
    private val noteListRepository: NoteRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val uiState = ObservableField(UIState.LOADING)

    private var notes = noteListRepository.notes

    val sortedNotes: LiveData<List<NoteEntry>> = getSavedFilter().switchMap { filter ->
        when (filter) {
            TODAY -> Transformations.map(notes) { noteList ->
                noteList.map { it }
                    .filter { it.reminder != null && DateUtils.isToday(it.reminder!!) }
            }
            UPCOMING -> Transformations.map(notes) { noteList ->
                noteList.map { it }
                    .filter { it.reminder != null && it.reminder!! > currentDate().timeInMillis }
            }
            COMPLETED -> Transformations.map(notes) { noteList ->
                noteList.map { it }
                    .filter { it.reminder != null && it.reminder!! < currentDate().timeInMillis }
            }
            else -> notes
        }
    }

    val notesToDelete: LiveData<List<NoteEntry>>
        get() = getNotesToDelete()

    fun deleteAllNotes(noteList: List<NoteEntry>) {
        val idList = ArrayList<Int>()
        for (note in noteList) {
            if (note.started && note.reminder!! > currentDate().timeInMillis) {
                cancelAlarm(context, note)
            }
            idList.add(note.id!!)
        }
        viewModelScope.launch {
            noteListRepository.deleteNotes(idList)
        }
    }

    fun deleteNote(note: NoteEntry) {
        if (note.started && note.reminder!! > currentDate().timeInMillis) {
            cancelAlarm(context, note)
        }
        viewModelScope.launch {
            noteListRepository.deleteNote(note.id!!)
        }
    }

    fun insertNote(note: NoteEntry) {
        if (note.started && note.reminder!! > currentDate().timeInMillis) {
            createSchedule(context, note)
        }
        viewModelScope.launch {
            noteListRepository.insertNote(note)
        }
    }

    fun insertNotes(noteList: List<NoteEntry>) {
        for (note in noteList) {
            if (note.started && note.reminder!! > currentDate().timeInMillis) {
                createSchedule(context, note)
            }
        }
        viewModelScope.launch {
            noteListRepository.insertNotes(noteList)
        }
    }

    fun setFilter(num: Int) {
        savedStateHandle.set(FILTER_SAVED_STATE_KEY, num)
    }

    private fun getSavedFilter(): MutableLiveData<Int> {
        return savedStateHandle.getLiveData(FILTER_SAVED_STATE_KEY, NO_FILTER)
    }

    fun setNotesToDelete(noteList: List<NoteEntry>) {
        savedStateHandle.set(NOTES_TO_DELETE, noteList)
    }

    private fun getNotesToDelete(): MutableLiveData<List<NoteEntry>> {
        return savedStateHandle.getLiveData(NOTES_TO_DELETE)
    }

    companion object {
        private const val TODAY = 1
        private const val UPCOMING = 2
        private const val COMPLETED = 3
        private const val NO_FILTER = 4
        private const val FILTER_SAVED_STATE_KEY = "FILTER_SAVED_STATE_KEY"
        private const val NOTES_TO_DELETE = "NOTES_TO_DELETE"
    }
}