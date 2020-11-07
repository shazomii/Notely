package com.davenet.notely.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.davenet.notely.database.getDatabase
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.repository.NotesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class EditNoteViewModel(selectedNote: NoteEntry?, application: Application) :
    AndroidViewModel(application) {
    private val noteRepository = NotesRepository(getDatabase(application))
    private var viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _noteBeingModified = MutableLiveData<NoteEntry?>()
    val noteBeingModified: LiveData<NoteEntry?> get() = _noteBeingModified

    private var _mIsEdit = MutableLiveData<Boolean>()
    val mIsEdit: LiveData<Boolean> get() = _mIsEdit

    init {
        if (selectedNote != null) {
            _noteBeingModified.value = selectedNote
           _mIsEdit.value = true
        } else {
            _noteBeingModified.value = noteRepository.emptyNote
            _mIsEdit.value = false
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
        if (!_mIsEdit.value!!) {
            insertNote(_noteBeingModified.value!!)
        } else {
            updateNote(_noteBeingModified.value!!)
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