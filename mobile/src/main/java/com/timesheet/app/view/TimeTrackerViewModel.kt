package com.timesheet.app.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.timesheet.app.data.dao.TimeTrackerDao

class TimeTrackerViewModel(
    timeTrackerDao: TimeTrackerDao,
    uid: Int
): ViewModel() {

    class Factory(val uid: Int): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            // Get the Application object from extras
            val application = checkNotNull(extras[APPLICATION_KEY])
            // Create a SavedStateHandle for this ViewModel from extras
            val savedStateHandle = extras.createSavedStateHandle()

            return TimeTrackerViewModel(
                (application as MyApplication).timeTrackerDao,
                uid
            ) as T
        }
    }

}