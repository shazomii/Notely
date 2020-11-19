package com.davenet.notely.viewmodels

import android.app.Application
import android.content.Context
import android.widget.TextView
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.davenet.notely.database.getDatabase
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.repository.NoteRepository
import com.davenet.notely.util.ReminderState
import com.davenet.notely.util.currentDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class EditNoteViewModel(selectedNoteId: Int?, application: Application) :
    AndroidViewModel(application) {
    private var selectedNote: NoteEntry?
    private var viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val reminderState = ObservableField(ReminderState.NO_REMINDER)

    private var _noteBeingModified = MutableLiveData<NoteEntry?>()
    val noteBeingModified: LiveData<NoteEntry?> get() = _noteBeingModified

    private var _mIsEdit = MutableLiveData<Boolean>()
    val mIsEdit: LiveData<Boolean> get() = _mIsEdit

    private val noteRepository = NoteRepository(getDatabase(application))


    init {
        if (selectedNoteId != null) {
            onNoteInserted()
            _noteBeingModified = noteRepository.getSelectedNote(selectedNoteId)
            selectedNote = _noteBeingModified.value
        } else {
            onNewNote()
            selectedNote = noteRepository.emptyNote
            _noteBeingModified.value = selectedNote
        }
    }

    var isChanged: Boolean = false
        get() = if (_mIsEdit.value!!) {
            _noteBeingModified.value != selectedNote
        } else {
            _noteBeingModified.value != noteRepository.emptyNote
        }
        private set

    fun pickDate(context: Context, note: NoteEntry, reminder: TextView) {
        viewModelScope.launch {
            noteRepository.pickDateTime(context, note, reminder)
        }
    }

    fun scheduleReminder(context: Context, note: NoteEntry) {
        viewModelScope.launch {
            noteRepository.schedule(context, note)
            updateNote(note)
        }
    }

    fun cancelReminder(context: Context, note: NoteEntry) {
        noteRepository.cancelAlarm(context, note)
    }

    fun saveNote() {
        if (!_mIsEdit.value!!) {
            insertNote(_noteBeingModified.value!!)
        } else {
            updateNote(_noteBeingModified.value!!)
        }
    }

    private fun insertNote(note: NoteEntry) {
        viewModelScope.launch {
            val newNote = note.copy(date = currentDate().timeInMillis)
            noteRepository.insertNote(newNote)
        }
    }

    private fun updateNote(note: NoteEntry) {
        viewModelScope.launch {
            val updatedNote = note.copy(date = currentDate().timeInMillis)
            noteRepository.updateNote(updatedNote)
        }
    }

    private fun onNoteInserted() {
        _mIsEdit.value = true
    }

    private fun onNewNote() {
        _mIsEdit.value = false
    }
}

class EditNoteViewModelFactory(
    private val application: Application,
    private val selectedNoteId: Int?
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditNoteViewModel::class.java)) {
            return EditNoteViewModel(selectedNoteId, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}