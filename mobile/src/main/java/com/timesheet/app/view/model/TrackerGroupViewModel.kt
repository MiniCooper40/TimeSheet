package com.timesheet.app.view.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.timesheet.app.application.MyApplication
import com.timesheet.app.data.dao.TimeTrackerDao
import com.timesheet.app.data.entity.GroupWithTrackers
import com.timesheet.app.data.entity.TrackerGroup
import com.timesheet.app.data.repository.TimeSheetPreferencesRepository
import com.timesheet.app.data.repository.TimeTrackerRepository
import com.timesheet.app.ui.table.ChartDataSorter
import com.timesheet.app.view.data.TimeSheetChartData
import com.timesheet.app.view.data.TimeTrackerChartData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class TrackerGroupViewModel(
    private val timeTrackerDao: TimeTrackerDao,
    private val timeTrackerRepository: TimeTrackerRepository,
    private val uid: Int
): ViewModel() {

    private val _trackerGroup = MutableStateFlow(GroupWithTrackers(
        TrackerGroup(""),
        listOf()
    ))
    val trackerGroup = _trackerGroup.asStateFlow()

    private val _trackerChartData = MutableStateFlow(TimeTrackerChartData(listOf()))
    val trackerChartData = _trackerChartData.asStateFlow()

    init {
        updateState()
    }

    fun sortTrackerChartDataBy(chartDataSorter: ChartDataSorter) {
        _trackerChartData.value = chartDataSorter.sort(_trackerChartData.value)
    }

    private fun getTrackerUids(): List<Int> = _trackerGroup.value.trackers.map { it.uid }
    private fun updateState() {
        viewModelScope.launch {
            _trackerGroup.value = timeTrackerDao.getTrackerGroupByGroupUid(uid)

            val currentTime = ZonedDateTime.now()

            val endTime = currentTime.withHour(23).withMinute(59).withSecond(59)
            val startTime = currentTime.minusWeeks(1).withHour(0).withMinute(0).withSecond(0)

            val trackerUids = getTrackerUids()
            println("tracker uids before $trackerUids")

            val chartData = timeTrackerRepository.timeTrackedBetween(startTime, endTime, trackerUids)

            println("tracker uids after ${chartData.tracked.map { it.timeTracker.uid }}")
            _trackerChartData.value = chartData
        }
    }

    companion object {

        fun factoryFor(uid: Int): ViewModelProvider.Factory {
            val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    // Get the Application object from extras
                    val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                    // Create a SavedStateHandle for this ViewModel from extras
                    val savedStateHandle = extras.createSavedStateHandle()

                    return TrackerGroupViewModel(
                        (application as MyApplication).timeTrackerDao,
                        TimeTrackerRepository(
                            (application as MyApplication).timeTrackerDao,
                            (application as MyApplication).trackedTimeDao
                        ),
                        uid
                    ) as T
                }
            }
            return factory
        }
    }
}