package com.timesheet.app.view.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.timesheet.app.data.repository.TimeTrackerRepository
import com.timesheet.app.ui.table.ChartDataSorter
import com.timesheet.app.application.MyApplication
import com.timesheet.app.data.dao.TimeTrackerDao
import com.timesheet.app.view.data.TimeTrackerChartData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomePageViewModel(
    val timeTrackerDao: TimeTrackerDao,
    val timeTrackerRepository: TimeTrackerRepository
): ViewModel() {




    companion object {

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                // Create a SavedStateHandle for this ViewModel from extras
                val savedStateHandle = extras.createSavedStateHandle()

                return HomePageViewModel(
                    (application as MyApplication).timeTrackerDao,
                    TimeTrackerRepository(
                        (application as MyApplication).timeTrackerDao,
                        (application as MyApplication).trackedTimeDao
                    )
                ) as T
            }
        }
    }
}