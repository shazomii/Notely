package com.davenet.notely.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.davenet.notely.database.DatabaseNote
import com.davenet.notely.database.NoteDao
import kotlinx.coroutines.*

class EditNoteViewModel(private val selectedNote: DatabaseNote?, val database: NoteDao) :
    ViewModel() {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val noteBeingModified: DatabaseNote

    private var mIsEdit: Boolean = false

    init {
        if (selectedNote != null) {
            noteBeingModified = selectedNote.copy()
            mIsEdit = true
            Log.d("editNote", "editnote")
        } else {
            noteBeingModified = emptyNote
            mIsEdit = false
            Log.d("editNote", "newnote")
        }
    }

    private suspend fun insertNote(note: DatabaseNote) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.insert(note)
            }
        }

        Log.d("editnote", "new note created in db")
    }

    private suspend fun updateNote(note: DatabaseNote) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.update(note)
            }
        }

        Log.d("editnote", "note updated in db")
    }

    suspend fun saveNote() {
        if (!mIsEdit) {
            insertNote(noteBeingModified)
        } else {
            updateNote(noteBeingModified)
        }
    }

    private val emptyNote: DatabaseNote
        get() {
            return DatabaseNote(title = "", text = "")
        }

//    var isChanged : Boolean = false
//        get() = if (mIsEdit) noteBeingModified != selectedNote
//                    else noteBeingModified != emptyNote
//        private set
}

class EditNoteViewModelFactory(
    private val database: NoteDao,
    private val selectedNote: DatabaseNote?
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditNoteViewModel::class.java)) {
            return EditNoteViewModel(selectedNote, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}