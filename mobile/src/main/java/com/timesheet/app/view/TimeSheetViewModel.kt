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
import com.timesheet.app.data.model.TrackedTime
import com.timesheet.app.view.model.TimeSheetUiState
import com.timesheet.app.view.model.TimeTrackerUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private lateinit var currentTracker: TimeTracker
    private lateinit var currentTrackerFlow: Flow<TimeTrackerUiState>
//    private lateinit var currentTrackerFlow: MutableStateFlow<TimeTrackerUiState>

    init {
        updateState()
    }

    fun trackedTimesFor(uid: Int): Flow<TimeTrackerUiState> {
        return flow {
            emit(
                TimeTrackerUiState(
                    timeTrackerDao.getTrackedTimesByUid(uid)
                )
            )
        }.also { currentTrackerFlow = it }
//        this.currentTrackerFlow = MutableStateFlow(TimeTrackerUiState())
//
//        return MutableStateFlow(TimeTrackerUiState()).also {flow->
//            viewModelScope.launch {
//                flow.value = TimeTrackerUiState(
//                    timeTrackerDao.getTrackedTimesByUid(uid)
//                )
//                currentTracker = timeTrackerDao.selectByUid(uid)
//                currentTrackerFlow = flow
//            }
//        }
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

    fun updateTrackerStartTime(context: Context, updatedTracker: TimeTracker) {
        Log.v("tracker", updatedTracker.toString())

        val currentTime = System.currentTimeMillis()
        var newStartTime = if(updatedTracker.startTime == 0L) currentTime else 0L // Start / End
        val endTime = currentTime
        val uid = updatedTracker.uid

        viewModelScope.launch {
            runBlocking {
                val numTrackers = timeTrackerDao.numberOfTrackersForUid(uid)
//                if(numTrackers == 0 && newStartTime == 0L) newStartTime = System.currentTimeMillis()

                val tracker = timeTrackerDao.selectByUid(uid)

                val trackers = timeTrackerDao.getTrackedTimesByUid(tracker.uid)

                Log.v("TRACKERS FOUND", numTrackers.toString())

                Log.v("tracker sync", tracker.toString())

                if(newStartTime == 0L) {
                    val trackedTime = TrackedTime(
                        startTime = tracker.startTime,
                        endTime = endTime,
                        trackerUid = tracker.uid
                    )
                    Log.v("Inserting", trackedTime.toString())
                    trackedTimeDao.insert(trackedTime)
                }

                timeTrackerDao.update(tracker.copy(startTime=newStartTime))

                updateState()
//                if(updatedTracker.uid == currentTracker.uid) {
//                    currentTrackerFlow.value = TimeTrackerUiState(
//                        timeTrackerDao.getTrackedTimesByUid(uid)
//                    )
//                }

            }
        }
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