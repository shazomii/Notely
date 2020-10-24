package com.davenet.notely.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.davenet.notely.database.getDatabase
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.repository.NotesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class EditNoteViewModel(private val selectedNote: NoteEntry?, application: Application) :
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
        } else {
            noteBeingModified = noteRepository.emptyNote
            mIsEdit = false
        }
    }

    private fun insertNote(note: NoteEntry) {
        viewModelScope.launch {
            noteRepository.insertNote(note)
        }
    }

    private fun updateNote(note: NoteEntry) {
        viewModelScope.launch {
            noteRepository.updateNote(note)
        }
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