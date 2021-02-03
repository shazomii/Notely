package com.davenet.notely.ui.notelist

import android.content.Context
import android.text.format.DateUtils
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.databinding.ObservableField
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.davenet.notely.database.asDomainModel
import com.davenet.notely.database.toDatabaseList
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.repository.NoteRepository
import com.davenet.notely.util.UIState
import com.davenet.notely.util.currentDate
import com.davenet.notely.work.cancelAlarm
import com.davenet.notely.work.createSchedule
import kotlinx.coroutines.launch

/**
 * The ViewModel for [NoteListFragment]
 */
class NoteListViewModel @ViewModelInject internal constructor(
    private val context: Context,
    private val noteListRepository: NoteRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val uiState = ObservableField(UIState.LOADING)

    private val notes = noteListRepository.notes


    val filteredNotes: LiveData<List<NoteEntry>> = getSavedFilter().switchMap { filter ->
        when (filter) {
            TODAY -> Transformations.map(notes) { noteList ->
                noteList.map { it }
                    .filter { it.reminder != null && DateUtils.isToday(it.reminder!!) }
            }
            UPCOMING -> Transformations.map(notes) { noteList ->
                noteList.map { it }
                    .filter { it.reminder != null && it.reminder!! > currentDate().timeInMillis }
            }
            COMPLETED -> Transformations.map(notes) { noteList ->
                noteList.map { it }
                    .filter { it.reminder != null && it.reminder!! < currentDate().timeInMillis }
            }
            else -> notes
        }
    }

    val notesToDelete: LiveData<List<NoteEntry>>
        get() = getNotesToDelete()

    /**
     * Delete the notes with the included ids from the database. Also, cancel
     * any active reminders associated with the notes.
     */
    fun deleteNotes() {
        val idList = ArrayList<Int>()
        getNotesToDelete().value?.let { noteList ->
            for (note in noteList) {
                if (note.started && note.reminder!! > currentDate().timeInMillis) {
                    cancelAlarm(context, note)
                }
                idList.add(note.id!!)
            }
            deleteTheNotes(idList)
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    fun deleteTheNotes(idList: ArrayList<Int>) {
        viewModelScope.launch {
            noteListRepository.deleteNotes(idList)
        }
    }

    /**
     * Delete a note from the database and cancel the active reminder associated
     * with it, if any.
     */
    fun deleteNote() {
        getNotesToDelete().value?.let { noteList ->
            if (noteList.first().started && noteList.first().reminder!! > currentDate().timeInMillis) {
                cancelAlarm(context, noteList.first())
            }
            deleteTheNote(noteList.first().id!!)
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    fun deleteTheNote(noteId: Int) {
        viewModelScope.launch {
            noteListRepository.deleteNote(noteId)
        }
    }

    /**
     * Insert a note into the database and create a reminder if it has one which
     * has not elapsed.
     */
    fun insertNote() {
        getNotesToDelete().value?.let { noteList ->
            if (noteList.first().started && noteList.first().reminder!! > currentDate().timeInMillis) {
                createSchedule(context, noteList.first())
            }
            insertTheNote(noteList.first())
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    fun insertTheNote(noteEntry: NoteEntry) {
        viewModelScope.launch {
            noteListRepository.insertNote(noteEntry)
        }
    }

    /**
     * Insert a list of notes into the database and create reminders for those with
     * valid reminders.
     */
    fun insertNotes() {
        getNotesToDelete().value?.let { noteList ->
            for (note in noteList) {
                if (note.started && note.reminder!! > currentDate().timeInMillis) {
                    createSchedule(context, note)
                }
            }
            insertTheNotes(noteList)
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    fun insertTheNotes(noteList: List<NoteEntry>) {
        viewModelScope.launch {
            noteListRepository.insertNotes(noteList)
        }
    }

    fun setFilter(num: Int) {
        savedStateHandle.set(FILTER_SAVED_STATE_KEY, num)
    }

    private fun getSavedFilter(): MutableLiveData<Int> {
        return savedStateHandle.getLiveData(FILTER_SAVED_STATE_KEY, NO_FILTER)
    }

    fun setNotesToDelete(noteList: List<NoteEntry>) {
        savedStateHandle.set(NOTES_TO_DELETE, noteList.toDatabaseList().asDomainModel())
    }

    private fun getNotesToDelete(): MutableLiveData<List<NoteEntry>> {
        return savedStateHandle.getLiveData(NOTES_TO_DELETE)
    }

    companion object {
        private const val TODAY = 1
        private const val UPCOMING = 2
        private const val COMPLETED = 3
        private const val NO_FILTER = 4
        private const val FILTER_SAVED_STATE_KEY = "FILTER_SAVED_STATE_KEY"
        private const val NOTES_TO_DELETE = "NOTES_TO_DELETE"
    }
}