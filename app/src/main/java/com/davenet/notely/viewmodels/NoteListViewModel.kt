package com.davenet.notely.viewmodels

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import androidx.databinding.ObservableField
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.repository.NoteRepository
import com.davenet.notely.util.UIState
import com.davenet.notely.util.currentDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NoteListViewModel @ViewModelInject internal constructor(
    private val noteListRepository: NoteRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val uiState = ObservableField(UIState.LOADING)

    private var viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

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

    fun deleteAllNotes(context: Context, noteList: List<NoteEntry>) {
        for (note in noteList) {
            if (note.started && note.reminder!! > currentDate().timeInMillis) {
                noteListRepository.cancelAlarm(context, note)
            }
        }
        viewModelScope.launch {
            noteListRepository.deleteAllNotes()
        }
    }

    fun deleteNote(context: Context, note: NoteEntry) {
        if (note.started && note.reminder!! > currentDate().timeInMillis) {
            noteListRepository.cancelAlarm(context, note)
        }
        viewModelScope.launch {
            noteListRepository.deleteNote(note.id!!)
        }
    }

    fun restoreNote(context: Context, note: NoteEntry) {
        if (note.started && note.reminder!! > currentDate().timeInMillis) {
            noteListRepository.createSchedule(context, note)
        }
        viewModelScope.launch {
            noteListRepository.insertNote(note)
        }
    }

    fun insertAllNotes(context: Context, noteList: List<NoteEntry>) {
        for (note in noteList) {
            if (note.started && note.reminder!! > currentDate().timeInMillis) {
                noteListRepository.createSchedule(context, note)
            }
        }
        viewModelScope.launch {
            noteListRepository.insertAllNotes(noteList)
        }
    }

    fun setFilter(num: Int) {
        savedStateHandle.set(FILTER_SAVED_STATE_KEY, num)
    }

    private fun getSavedFilter(): MutableLiveData<Int> {
        Log.d("filter", savedStateHandle.getLiveData(FILTER_SAVED_STATE_KEY, NO_FILTER).value.toString())
        return savedStateHandle.getLiveData(FILTER_SAVED_STATE_KEY, NO_FILTER)
    }

    companion object {
        private const val TODAY = 1
        private const val UPCOMING = 2
        private const val COMPLETED = 3
        private const val NO_FILTER = 4
        private const val FILTER_SAVED_STATE_KEY = "FILTER_SAVED_STATE_KEY"
    }
}