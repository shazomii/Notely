package com.davenet.notely.viewmodels

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.davenet.notely.database.getDatabase
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.repository.NoteListRepository
import com.davenet.notely.util.UIState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NoteListViewModel(application: Application) : AndroidViewModel(application) {
    private val noteListRepository = NoteListRepository(getDatabase(application))

    val uiState = ObservableField(UIState.LOADING)

    private var viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _navigateToNoteDetail = MutableLiveData<Int?>()
    val navigateToNoteDetail: LiveData<Int?> get() = _navigateToNoteDetail

    var notes = noteListRepository.notes

    fun onNoteClicked(noteId: Int?) {
        _navigateToNoteDetail.value = noteId
    }

    fun deleteAllNotes() {
        viewModelScope.launch {
            noteListRepository.deleteAllNotes()
        }
    }

    fun deleteNote(note: NoteEntry) {
        viewModelScope.launch {
            noteListRepository.deleteNote(note)
        }
    }

    fun restoreNote(note: NoteEntry) {
        viewModelScope.launch {
            noteListRepository.restoreNote(note)
        }
    }

    fun insertAllNotes(noteList: List<NoteEntry>) {
        viewModelScope.launch {
            noteListRepository.insertAllNotes(noteList)
        }
    }
}

class NoteListViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteListViewModel::class.java)) {
            return NoteListViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}