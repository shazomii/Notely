package com.davenet.notely.viewmodels

import android.app.Activity
import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.davenet.notely.database.asDomainModelEntry
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.repository.NoteRepository
import com.davenet.notely.util.ReminderCompletion
import com.davenet.notely.util.ReminderState
import com.davenet.notely.util.currentDate
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class EditNoteViewModel @AssistedInject constructor(private val noteRepository: NoteRepository, @Assisted private val selectedNoteId: Int?) :
    ViewModel() {
    private lateinit var selectedNote: NoteEntry
    private lateinit var scheduledNote: NoteEntry

    val reminderState = ObservableField(ReminderState.NO_REMINDER)
    val reminderCompletion = ObservableField(ReminderCompletion.ONGOING)

    private var viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _noteBeingModified = MutableLiveData<NoteEntry?>()
    val noteBeingModified: LiveData<NoteEntry?> get() = _noteBeingModified

    private var _mIsEdit = MutableLiveData<Boolean>()
    val mIsEdit: LiveData<Boolean> get() = _mIsEdit

    init {
        if (selectedNoteId == -1) {
            onNewNote()
            selectedNote = noteRepository.emptyNote
            _noteBeingModified.value = selectedNote
        } else {
            onNoteInserted()
            getSelectedNote()
        }
    }

    private fun getSelectedNote() {
        runBlocking {
            noteRepository.getNote(selectedNoteId!!).collect { noteEntry ->
                _noteBeingModified.value = noteEntry
                selectedNote = noteEntry!!.copy().asDomainModelEntry()
            }
        }
    }

    private fun getUpdatedNote() {
        runBlocking {
            noteRepository.getLatestNote().collect { noteEntry ->
                scheduledNote = noteEntry!!
            }
        }
    }

    private val _isChanged: MutableLiveData<Boolean>
        get() = if (_mIsEdit.value!!) {
            MutableLiveData(_noteBeingModified.value != selectedNote)
        } else {
            MutableLiveData(_noteBeingModified.value != noteRepository.emptyNote)
        }

    val isChanged: LiveData<Boolean> get() = _isChanged

    fun setDateTime(dateTime: Long) {
        _noteBeingModified.value = _noteBeingModified.value!!.copy(reminder = dateTime)
    }

    fun pickColor(activity: Activity, note: NoteEntry) {
        noteRepository.pickColor(activity, note)
    }

    fun scheduleReminder(context: Context, note: NoteEntry) {
        if (_noteBeingModified.value!!.reminder != null && _noteBeingModified.value!!.reminder!! > currentDate().timeInMillis) {
            if (_mIsEdit.value!!) {
                noteRepository.createSchedule(context, note)
                updateNote(note)
            } else {
                getUpdatedNote()
                noteRepository.createSchedule(context, scheduledNote)
                updateNote(scheduledNote)
            }
            reminderCompletion.set(ReminderCompletion.ONGOING)
        }
    }

    fun deleteNote(context: Context, note: NoteEntry) {
        if (note.started) {
            cancelReminder(context, note)
        }
        viewModelScope.launch {
            noteRepository.deleteNote(note.id!!)
        }
    }

    fun cancelReminder(context: Context, note: NoteEntry) {
        _noteBeingModified.value = _noteBeingModified.value!!.copy(reminder = null, started = false)
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
        val newNote = note.copy(date = currentDate().timeInMillis)
        runBlocking {
            noteRepository.insertNote(newNote)
        }
    }

    private fun updateNote(note: NoteEntry) {
        val updatedNote = note.copy(date = currentDate().timeInMillis)
        runBlocking {
            noteRepository.updateNote(updatedNote)
        }
    }

    private fun onNoteInserted() {
        _mIsEdit.value = true
    }

    private fun onNewNote() {
        _mIsEdit.value = false
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(selectedNoteId: Int?): EditNoteViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            selectedNoteId: Int?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(selectedNoteId) as T
            }
        }
    }
}