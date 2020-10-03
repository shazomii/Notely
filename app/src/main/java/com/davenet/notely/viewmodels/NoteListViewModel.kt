package com.davenet.notely.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.davenet.notely.database.DatabaseNote
import com.davenet.notely.database.NoteDao
import kotlinx.coroutines.*

class NoteListViewModel(
    val database: NoteDao,
    application: Application
) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _navigateToNoteDetail = MutableLiveData<DatabaseNote?>()
    val navigateToNoteDetail: LiveData<DatabaseNote?> get() = _navigateToNoteDetail

    var notes = database.getAllNotes()

    fun onNoteDetailNavigated() {
        _navigateToNoteDetail.value = null
    }

    fun onNoteClicked(note: DatabaseNote) {
        _navigateToNoteDetail.value = note
    }

    suspend fun deleteAllNotes() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.deleteAllNotes()
            }
        }
    }

    suspend fun deleteNote(note: DatabaseNote) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.deleteNote(note.id)
            }
        }
        Log.d("notelist", "note removed from db")
    }

    suspend fun insertNote(note: DatabaseNote) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.insert(note)
            }
        }
        Log.d("notelist", "note replaced into db")
    }

    suspend fun insertAllNotes(noteList: List<DatabaseNote>) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.insertNotesList(noteList)
            }
        }
    }
}

class NoteListViewModelFactory(
    private val dataSource: NoteDao,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteListViewModel::class.java)) {
            return NoteListViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}