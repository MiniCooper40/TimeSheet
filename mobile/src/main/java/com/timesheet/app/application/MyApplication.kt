package com.timesheet.app.application

import android.app.Application
import com.timesheet.app.data.db.TimeSheetDatabase

class MyApplication: Application() {

    val timeSheetDatabase by lazy { TimeSheetDatabase.getDatabase(this) }

    val timeTrackerDao by lazy { timeSheetDatabase.timeTrackerDao() }
    val trackedTimesDao by lazy { timeSheetDatabase.trackedTimesDao() }
    val trackedTimeDao by lazy { timeSheetDatabase.trackedTimeDao() }
}