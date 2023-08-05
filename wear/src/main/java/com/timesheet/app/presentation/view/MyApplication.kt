package com.timesheet.app.presentation.view

import android.app.Application
import com.timesheet.app.presentation.data.dao.TimeTrackerDao
import com.timesheet.app.presentation.data.db.TimeSheetDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MyApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created
    // when they're needed rather than when the application starts
    val timeSheetDatabase by lazy { TimeSheetDatabase.getDatabase(this) }

    val timeSheetDao by lazy { timeSheetDatabase.timeTrackerDao() }
}