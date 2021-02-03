package com.davenet.notely.ui.editnote

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.davenet.notely.repository.NoteRepository
import com.davenet.notely.testutils.rules.CoroutineRule
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule

class EditNoteViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutinesTestRule = CoroutineRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK(relaxed = true)
    lateinit var repository: NoteRepository

    @MockK(relaxed = true)
    lateinit var viewModel: EditNoteViewModel
}