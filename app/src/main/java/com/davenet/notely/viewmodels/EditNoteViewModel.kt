package com.davenet.notely.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.davenet.notely.database.asDomainModelEntry
import com.davenet.notely.database.getDatabase
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.repository.NoteRepository
import com.davenet.notely.util.ReminderCompletion
import com.davenet.notely.util.ReminderState
import com.davenet.notely.util.currentDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class EditNoteViewModel(private val selectedNoteId: Int?, application: Application) :
    AndroidViewModel(application) {
    private lateinit var selectedNote: NoteEntry
    private lateinit var scheduledNote: NoteEntry

    val reminderState = ObservableField(ReminderState.NO_REMINDER)
    val reminderCompletion = ObservableField(ReminderCompletion.ONGOING)

    private var _noteBeingModified = MutableLiveData<NoteEntry?>()
    val noteBeingModified: LiveData<NoteEntry?> get() = _noteBeingModified

    private var _mIsEdit = MutableLiveData<Boolean>()

    private val noteRepository = NoteRepository(getDatabase(application))

    init {
        if (selectedNoteId != null) {
            onNoteInserted()
            getSelectedNote()
        } else {
            onNewNote()
            selectedNote = noteRepository.emptyNote
            _noteBeingModified.value = selectedNote
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
}

@ExperimentalCoroutinesApi
class EditNoteViewModelFactory(
    private val application: Application,
    private val selectedNoteId: Int?
) : ViewModelProvider.NewInstanceFactory() {
    @InternalCoroutinesApi
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditNoteViewModel::class.java)) {
            return EditNoteViewModel(selectedNoteId, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}