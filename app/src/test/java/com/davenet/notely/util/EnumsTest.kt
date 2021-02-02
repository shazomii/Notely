package com.davenet.notely.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnumsTest {
    lateinit var uiState: UIState
    lateinit var reminderAvailableState: ReminderAvailableState
    lateinit var reminderCompletionState: ReminderCompletionState

    @Test
    fun setUIStateAsEmpty_ShouldBeEmpty() {
        uiState = UIState.EMPTY

        assertTrue(uiState == UIState.EMPTY)
        assertFalse(uiState == UIState.LOADING)
    }

    @Test
    fun setUIStateAsLoading_ShouldBeLoading() {
        uiState = UIState.LOADING

        assertTrue(uiState == UIState.LOADING)
        assertFalse(uiState == UIState.EMPTY)
    }

    @Test
    fun setUIStateAsHasData_ShouldBeHasData() {
        uiState = UIState.HAS_DATA

        assertTrue(uiState == UIState.HAS_DATA)
        assertFalse(uiState == UIState.EMPTY)
    }

    @Test
    fun setAvailableStateAsHasReminder_ShouldBeHasReminder() {
        reminderAvailableState = ReminderAvailableState.HAS_REMINDER

        assertTrue(reminderAvailableState == ReminderAvailableState.HAS_REMINDER)
        assertFalse(reminderAvailableState == ReminderAvailableState.NO_REMINDER)
    }

    @Test
    fun setAvailableStateAsNoReminder_ShouldBeNoReminder() {
        reminderAvailableState = ReminderAvailableState.NO_REMINDER

        assertTrue(reminderAvailableState == ReminderAvailableState.NO_REMINDER)
        assertFalse(reminderAvailableState == ReminderAvailableState.HAS_REMINDER)
    }

    @Test
    fun setCompletionStateAsOngoing_ShouldBeOngoing() {
        reminderCompletionState = ReminderCompletionState.ONGOING

        assertTrue(reminderCompletionState == ReminderCompletionState.ONGOING)
        assertFalse(reminderCompletionState == ReminderCompletionState.COMPLETED)
    }

    @Test
    fun setCompletionStateAsCompleted_ShouldBeCompleted() {
        reminderCompletionState = ReminderCompletionState.COMPLETED

        assertTrue(reminderCompletionState == ReminderCompletionState.COMPLETED)
        assertFalse(reminderCompletionState == ReminderCompletionState.ONGOING)
    }
}