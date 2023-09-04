package com.timesheet.app.view.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.timesheet.app.data.repository.TimeTrackerRepository
import com.timesheet.app.ui.table.TableSortType
import com.timesheet.app.application.MyApplication
import com.timesheet.app.view.data.TimeTrackerChartData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomePageViewModel(
    val timeTrackerRepository: TimeTrackerRepository
): ViewModel() {

    private val _lastWeekData = MutableStateFlow(TimeTrackerChartData(listOf())) //MutableStateFlow<Map<TimeTracker, Duration>>(mapOf())
    val lastWeekData = _lastWeekData.asStateFlow()

    init {
        updateLastWeekData()
    }

    private fun updateLastWeekData() {
        viewModelScope.launch {
            val today = LocalDate.now().plusDays(2)
            val weekAgo = today.minusWeeks(3)
            _lastWeekData.value = timeTrackerRepository.timeTrackedBetween(weekAgo, today)
        }
    }

    fun sortWeeklyDataByTime() {
        val currentData = _lastWeekData.value

        val newData = currentData.tracked.sortedByDescending { it.duration }

        Log.v("SORTED", currentData.tracked.toString())
        _lastWeekData.value = TimeTrackerChartData(newData)
    }

    fun sortWeeklyBy(tableSortType: TableSortType) {
        _lastWeekData.value = tableSortType.sort(_lastWeekData.value)
    }
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
                    TimeTrackerRepository(
                        (application as MyApplication).timeTrackerDao,
                        (application as MyApplication).trackedTimeDao
                    )
                ) as T
            }
        }
    }
}