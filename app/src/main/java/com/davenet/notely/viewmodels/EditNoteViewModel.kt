package com.davenet.notely.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.davenet.notely.database.getDatabase
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.repository.NotesRepository
import kotlinx.coroutines.*

class EditNoteViewModel(selectedNote: NoteEntry?, application: Application) :
    AndroidViewModel(application) {
    private val noteRepository = NotesRepository(getDatabase(application))
    private var viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val noteBeingModified: NoteEntry

    private var mIsEdit: Boolean = false

    init {
        if (selectedNote != null) {
            noteBeingModified = selectedNote
            mIsEdit = true
            Log.d("editNote", "editnote")
        } else {
            noteBeingModified = noteRepository.emptyNote
            mIsEdit = false
            Log.d("editNote", "newnote")
        }
    }

    private fun insertNote(note: NoteEntry) {
        viewModelScope.launch {
            noteRepository.insertNote(note)
        }

        Log.d("editnote", "new note created in db")
    }

    private fun updateNote(note: NoteEntry) {
        viewModelScope.launch {
            noteRepository.updateNote(note)
        }

        Log.d("editnote", "note updated in db")
    }

    fun saveNote() {
        if (!mIsEdit) {
            insertNote(noteBeingModified)
        } else {
            updateNote(noteBeingModified)
        }
    }
}

class EditNoteViewModelFactory(
    private val application: Application,
    private val selectedNote: NoteEntry?
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditNoteViewModel::class.java)) {
            return EditNoteViewModel(selectedNote, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}