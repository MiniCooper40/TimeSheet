package com.timesheet.app.view

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.timesheet.app.data.dao.TimeTrackerDao
import com.timesheet.app.data.dao.TrackedTimeDao
import com.timesheet.app.data.dao.TrackedTimesDao
import com.timesheet.app.data.model.TimeTracker
import com.timesheet.app.data.model.TrackedTimes
import com.timesheet.app.view.model.TimeSheetUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TimeSheetViewModel(
    val timeTrackerDao: TimeTrackerDao,
    val trackedTimeDao: TrackedTimeDao,
    val trackedTimesDao: TrackedTimesDao
): ViewModel() {

    private val _timeTrackers = MutableStateFlow(TimeSheetUiState(listOf()))
    val timeTrackers = _timeTrackers.asStateFlow()

    init {
        updateState()
    }

    fun trackedTimesFor(uid: Int): Flow<TrackedTimes> = flow {
            emit(
                timeTrackerDao.getTrackedTimesByUid(uid)
            )
    }
    private fun updateState() {
        viewModelScope.launch {
            _timeTrackers.value = TimeSheetUiState(
                timeTrackerDao.selectAll()
            )
        }
    }

    fun createTracker(context: Context, tracker: TimeTracker) {
        viewModelScope.launch {
            runBlocking {
                val uid = timeTrackerDao.insert(tracker).toInt()

                Log.v("after insert", tracker.toString())

                val putDataMapRequest = PutDataMapRequest.create("/tracker/create")

                val dataMap = putDataMapRequest.dataMap

                dataMap.putLong("created", System.currentTimeMillis())
                dataMap.putString("title", tracker.title)
                dataMap.putLong("startTime", tracker.startTime)
                dataMap.putInt("uid", uid)

                val dataRequest = putDataMapRequest.asPutDataRequest()

                Wearable.getDataClient(context).putDataItem(dataRequest)
            }
        }
        updateState()
    }
    companion object {

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])
                // Create a SavedStateHandle for this ViewModel from extras
                val savedStateHandle = extras.createSavedStateHandle()

                return TimeSheetViewModel(
                    (application as MyApplication).timeTrackerDao,
                    application.trackedTimeDao,
                    application.trackedTimesDao
                ) as T
            }
        }
    }
}