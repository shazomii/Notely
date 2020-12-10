package com.davenet.notely.viewmodels

import android.content.Context
import androidx.databinding.ObservableField
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.repository.NoteRepository
import com.davenet.notely.util.NoteFilter
import com.davenet.notely.util.UIState
import com.davenet.notely.util.currentDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NoteListViewModel @ViewModelInject internal constructor(private val noteListRepository: NoteRepository) : ViewModel() {
    val uiState = ObservableField(UIState.LOADING)
    val noteFilter = ObservableField(NoteFilter.ALL)

    private var viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

//    private val _navigateToNoteDetail = MutableLiveData<Int?>()
//    val navigateToNoteDetail: LiveData<Int?> get() = _navigateToNoteDetail

    var notes = noteListRepository.notes

//    fun onNoteClicked(noteId: Int?) {
//        _navigateToNoteDetail.value = noteId
//    }
//
//    fun onNoteDetailNavigated() {
//        _navigateToNoteDetail.value = null
//    }

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
}