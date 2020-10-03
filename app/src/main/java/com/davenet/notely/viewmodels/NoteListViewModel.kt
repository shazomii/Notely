package com.davenet.notely.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.davenet.notely.database.DatabaseNote
import com.davenet.notely.database.NoteDao
import com.davenet.notely.database.getDatabase
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.domain.asDataBaseModel
import com.davenet.notely.repository.NotesRepository
import kotlinx.coroutines.*

class NoteListViewModel(application: Application) : AndroidViewModel(application) {
    private val notesRepository = NotesRepository(getDatabase(application))

    private var viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _navigateToNoteDetail = MutableLiveData<NoteEntry?>()
    val navigateToNoteDetail: LiveData<NoteEntry?> get() = _navigateToNoteDetail

    var notes = notesRepository.notes

    fun onNoteDetailNavigated() {
        _navigateToNoteDetail.value = null
    }

    fun onNoteClicked(note: NoteEntry) {
        _navigateToNoteDetail.value = note
    }

    fun deleteAllNotes() {
        viewModelScope.launch {
            notesRepository.deleteAllNotes()
        }
    }

    fun deleteNote(note: NoteEntry) {
        viewModelScope.launch {
            notesRepository.deleteNote(note)
        }
        Log.d("notelist", "note removed from db")
    }

    fun insertNote(note: NoteEntry) {
        viewModelScope.launch {
            notesRepository.insertNote(note)
        }
        Log.d("notelist", "note replaced into db")
    }

    fun insertAllNotes(noteList: List<NoteEntry>) {
        viewModelScope.launch {
            notesRepository.insertAllNotes(noteList)
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