package com.timesheet.app.presentation.view

import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityClient.OnCapabilityChangedListener
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient.OnDataChangedListener
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.timesheet.app.presentation.data.dao.TimeTrackerDao
import com.timesheet.app.presentation.data.db.TimeSheetDatabase
import com.timesheet.app.presentation.data.model.TimeTracker
import com.timesheet.app.presentation.view.model.TimeSheetUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TimeSheetViewModel(
    val dao: TimeTrackerDao
): ViewModel(), OnDataChangedListener{

    private val _timeSheetState = MutableStateFlow(TimeSheetUiState(listOf()))

    val timeSheetState = _timeSheetState.asStateFlow()

    init {
        updateTimeSheetState()
    }

    fun updateTimeSheetState() {
        viewModelScope.launch {
            _timeSheetState.value =  TimeSheetUiState(dao.selectAll())
        }
    }

    fun updateTrackerStartTime(context: Context, updatedTracker: TimeTracker) {
        Log.v("tracker", updatedTracker.toString())
        val newStartTime = if(updatedTracker.startTime == 0L) System.currentTimeMillis() else 0L
        val endTime = if(updatedTracker.startTime == 0L) 0L else System.currentTimeMillis()
        viewModelScope.launch {
            dao.update(timeTracker = updatedTracker.copy(startTime = newStartTime))

            val putDataMapRequest = PutDataMapRequest.create("/tracked/create")

            val dataMap = putDataMapRequest.dataMap

            dataMap.putLong("startTime", newStartTime)
            dataMap.putLong("endTime", endTime)
            dataMap.putInt("uid", updatedTracker.uid)

            val dataRequest = putDataMapRequest.asPutDataRequest()

            Wearable.getDataClient(context).putDataItem(dataRequest)
        }
        updateTimeSheetState()
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

                return TimeSheetViewModel((application as MyApplication).timeSheetDao) as T
            }
        }
    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        updateTimeSheetState()
    }
}