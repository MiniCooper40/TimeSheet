package com.timesheet.app.listener

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.timesheet.app.data.dao.TimeTrackerDao
import com.timesheet.app.data.dao.TrackedTimeDao
import com.timesheet.app.data.db.TimeSheetDatabase
import com.timesheet.app.data.model.TimeTracker
import com.timesheet.app.data.model.TrackedTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TimeSheetWearableListenerService: WearableListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var timeTrackerDao: TimeTrackerDao
    private lateinit var trackedTimeDao: TrackedTimeDao
    init {
        val context: Context = this
        Log.v("WEARABLE LISTENER", "In INIT")
        scope.launch {
            val db = TimeSheetDatabase.getDatabase(context)
            timeTrackerDao = db.timeTrackerDao()
            trackedTimeDao = db.trackedTimeDao()
        }
    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        Log.v("WEARABLE LISTENER", "In wearable listener, onDataChanged")

        dataEventBuffer.forEach {
            it.dataItem.uri.also { Log.v("print", "uri = $it") }
            it.dataItem.uri.lastPathSegment.also{ Log.v("print", "lastPathSegment = $it") }

            if(it.dataItem.uri.path == "/tracked/create") {
                DataMapItem.fromDataItem(it.dataItem).dataMap.apply {

                    val startTime = this.getLong("startTime", 0)
                    val endTime = this.getLong("endTime", 0)
                    val uid = this.getInt("uid")

                    scope.launch {

                        runBlocking {
                            val tracker = timeTrackerDao.selectByUid(uid)

                            val trackers = timeTrackerDao.getTrackedTimesByUid(tracker.uid)

                            Log.v("TRACKERS FOUND", trackers.toString())

                            Log.v("tracker sync", tracker.toString())

                            if(startTime == 0L) {
                                val trackedTime = TrackedTime(
                                    startTime = tracker.startTime,
                                    endTime = endTime,
                                    trackerUid = tracker.uid
                                )
                                trackedTimeDao.insert(trackedTime)
                            }

                            timeTrackerDao.update(tracker.copy(startTime=startTime))
                        }
                    }

                    Log.v("print", this.toString())
                }
            }
        }
    }
}