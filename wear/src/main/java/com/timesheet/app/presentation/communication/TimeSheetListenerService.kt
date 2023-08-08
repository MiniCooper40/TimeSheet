package com.timesheet.app.presentation.communication

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.currentRecomposeScope
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.timesheet.app.presentation.data.dao.TimeTrackerDao
import com.timesheet.app.presentation.data.db.TimeSheetDatabase
import com.timesheet.app.presentation.data.model.TimeTracker
import com.timesheet.app.presentation.data.repository.TimeTrackerRepository
import com.timesheet.app.presentation.view.MyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class TimeSheetListenerService: WearableListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var dao: TimeTrackerDao
    init {
        val context: Context = this
        Log.v("WEARABLE LISTENER", "In INIT")
        scope.launch {
            val db = TimeSheetDatabase.getDatabase(context)
            dao = db.timeTrackerDao()
        }
    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        Log.v("WEARABLE LISTENER", "In wearable listener, onDataChanged")

        dataEventBuffer.forEach {
            it.dataItem.uri.also { Log.v("print", "uri = $it") }
            it.dataItem.uri.lastPathSegment.also{ Log.v("print", "lastPathSegment = $it") }
            DataEvent.TYPE_CHANGED
            when(it.type) {
                DataEvent.TYPE_CHANGED -> onTypeChangeEvent(it)
                else -> onDeleteEvent(it)
            }
        }
    }

    private fun createTracker(dataItem: DataItem) {
        DataMapItem.fromDataItem(dataItem).dataMap.apply {
            val startTime = this.getLong("startTime")
            val title = this.getString("title")
            val uid = this.getInt("uid")

            Log.v("CREATING", uid.toString())
            scope.launch {
                title?.let {
                    dao.insert(TimeTracker(title= it, startTime=startTime, uid = uid))
                }
            }
        }
    }

    private fun deleteTracker(dataItem: DataItem) {
        DataMapItem.fromDataItem(dataItem).dataMap.apply {
            val startTime = this.getLong("startTime")
            val title = this.getString("title")
            val uid = this.getInt("uid")
            Log.v("DELETING", uid.toString())
            scope.launch {
                title?.let{
                    dao.selectByUid(uid).also {
                        dao.delete(it)
                    }
                }
            }
        }
    }


    private fun updateTracker(dataItem: DataItem) {
        DataMapItem.fromDataItem(dataItem).dataMap.apply {
            val startTime = this.getLong("startTime")
            val title = this.getString("title")
            val uid = this.getInt("uid")
            Log.v("UPDATING", uid.toString())
            scope.launch {
                title?.let{
                    dao.selectByUid(uid).also {
                        dao.update(it.copy(title = title, startTime = startTime))
                    }
                }
            }
        }
    }


    private fun onTypeChangeEvent(dataEvent: DataEvent) {

        val dataItem = dataEvent.dataItem

        when(dataItem.uri.lastPathSegment) {
            "update"->updateTracker(dataItem)
            "create"->createTracker(dataItem)
            else -> deleteTracker(dataItem)
        }
    }

    private fun onDeleteEvent(dataEvent: DataEvent) {
        Log.v("WEARABLE LISTENER", "In wearable listener, onDeleteEvent")
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.v("WEARABLE LISTENER", "In wearable listener, onCapabilityChanged")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.v("WEARABLE LISTENER", "In wearable listener, onMessagedReceived")
    }
}