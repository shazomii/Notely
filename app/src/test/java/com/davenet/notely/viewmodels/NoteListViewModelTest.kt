package com.davenet.notely.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.repository.NoteRepository
import com.davenet.notely.testutils.rules.CoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NoteListViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutinesTestRule = CoroutineRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK(relaxed = true)
    lateinit var repository: NoteRepository

    @MockK(relaxed = true)
    private lateinit var viewModel: NoteListViewModel

    @MockK(relaxed = true)
    private lateinit var savedStateHandle: SavedStateHandle

    @MockK(relaxed = true)
    private lateinit var context: Context


    private val data = MutableLiveData<List<NoteEntry>>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every {
            repository.notes
        } returns data
        viewModel = NoteListViewModel(context, repository, savedStateHandle)
    }

    @Test
    fun initializeViewModel_ShouldObtainAllNotes() {
        coVerify {
            repository.notes
        }
    }

    @Test
    fun deleteNotes_ShouldInvokeRepositoryDeleteNotesMethod() {
        val idList: ArrayList<Int> = mockk()
        viewModel.deleteTheNotes(idList)

        coVerify {
            repository.deleteNotes(idList)
        }
    }

    @Test
    fun deleteNote_ShouldInvokeRepositoryDeleteNoteMethod() {
        val id = 0
        viewModel.deleteTheNote(id)

        coVerify {
            repository.deleteNote(id)
        }
    }

    @Test
    fun insertNote_ShouldInvokeRepositoryInsertNoteMethod() {
        val note = mockk<NoteEntry>()
        viewModel.insertTheNote(note)

        coVerify {
            repository.insertNote(note)
        }
    }

    @Test
    fun insertNotes_ShouldInvokeRepositoryInsertNotesMethod() {
        val notes = mockk<List<NoteEntry>>()
        viewModel.insertTheNotes(notes)

        coVerify {
            repository.insertNotes(notes)
        }
    }
}