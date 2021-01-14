package com.davenet.notely.viewmodels

import android.app.Activity
import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.davenet.notely.database.DatabaseNote.Companion.toDatabaseEntry
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.repository.NoteRepository
import com.davenet.notely.ui.editnote.EditNoteFragment
import com.davenet.notely.util.ReminderCompletion
import com.davenet.notely.util.ReminderState
import com.davenet.notely.util.currentDate
import com.davenet.notely.util.selectColor
import com.davenet.notely.work.cancelAlarm
import com.davenet.notely.work.createSchedule
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * The ViewModel for [EditNoteFragment]
 */
class EditNoteViewModel @AssistedInject constructor(
    private val noteRepository: NoteRepository,
    @Assisted private val selectedNoteId: Int?
) :
    ViewModel() {
    private lateinit var selectedNote: NoteEntry
    private lateinit var scheduledNote: NoteEntry

    val reminderState = ObservableField(ReminderState.NO_REMINDER)
    val reminderCompletion = ObservableField(ReminderCompletion.ONGOING)

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
            viewModelScope.launch {
                noteRepository.getNote(selectedNoteId!!).collect { noteEntry ->
                    _noteBeingModified.value = noteEntry
                    selectedNote = toDatabaseEntry(noteEntry!!).asDomainModelEntry()
                }
            }
        }
    }

    private val _isChanged: MutableLiveData<Boolean>
        get() = if (_mIsEdit.value!!) {
            MutableLiveData(_noteBeingModified.value != selectedNote)
        } else {
            MutableLiveData(_noteBeingModified.value != noteRepository.emptyNote.copy(color = _noteBeingModified.value?.color!!))
        }

    /**
     * Return true or false if the contents of a Note have changed or not respectively.
     */
    val isChanged: LiveData<Boolean> get() = _isChanged

    /**
     * Set the time and date for a Note's reminder
     *
     * @param dateTime [Long]
     */
    fun setDateTime(dateTime: Long) {
        _noteBeingModified.value = _noteBeingModified.value!!.copy(reminder = dateTime)
    }

    /**
     * Set a Note's color
     *
     * @param activity [Activity]
     * @param note [NoteEntry]
     */
    fun pickColor(activity: Activity, note: NoteEntry) {
        selectColor(activity, note)
    }

    /**
     * Check if a note includes a reminder and create the reminder if the time has not elapsed.
     *
     * @param context [Context]
     * @param note [NoteEntry]
     */
    fun scheduleReminder(context: Context, note: NoteEntry) {
        if (_noteBeingModified.value!!.reminder != null && _noteBeingModified.value!!.reminder!! > currentDate().timeInMillis) {
            if (_mIsEdit.value!!) {
                createSchedule(context, note)
                updateNote(note)
            } else {
                runBlocking {
                    noteRepository.getLatestNote().collect { noteEntry ->
                        scheduledNote = noteEntry!!
                    }
                }
                createSchedule(context, scheduledNote)
                updateNote(scheduledNote)
            }
            reminderCompletion.set(ReminderCompletion.ONGOING)
        }
    }

    /**
     * Delete a note from the database and cancel the active reminder
     * associated with it, if any.
     *
     * @param context [Context]
     * @param note [NoteEntry]
     */
    fun deleteNote(context: Context, note: NoteEntry) {
        if (note.started) {
            cancelReminder(context, note)
        }
        viewModelScope.launch {
            noteRepository.deleteNote(note.id!!)
        }
    }

    /**
     * Cancel an active reminder associated with a note.
     *
     * @param context [Context]
     * @param note [NoteEntry]
     */
    fun cancelReminder(context: Context, note: NoteEntry) {
        _noteBeingModified.value = _noteBeingModified.value!!.copy(reminder = null, started = false)
        cancelAlarm(context, note)
    }

    /**
     * Check if a note is being edited and update it if true. Insert a new note
     * in the database if false
     */
    fun saveNote() {
        if (!_mIsEdit.value!!) {
            insertNote(_noteBeingModified.value!!)
        } else {
            updateNote(_noteBeingModified.value!!)
        }
    }

    /**
     * Insert a single note into the database
     *
     * @param note [NoteEntry]
     */
    private fun insertNote(note: NoteEntry) {
        val newNote = note.copy(date = currentDate().timeInMillis)
        runBlocking {
            noteRepository.insertNote(newNote)
        }
    }

    /**
     * Update contents of a Note in the database
     *
     * @param note [NoteEntry]
     */
    private fun updateNote(note: NoteEntry) {
        val updatedNote = note.copy(date = currentDate().timeInMillis)
        viewModelScope.launch {
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