package com.timesheet.app.presentation.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.timesheet.app.presentation.data.dao.TimeTrackerDao
import com.timesheet.app.presentation.data.db.TimeSheetDatabase
import com.timesheet.app.presentation.data.model.TimeTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.withContext

class TimeTrackerRepository(application: Application) {

    private var timeTrackerDao: TimeTrackerDao

    init {
        val db = TimeSheetDatabase.getDatabase(application.applicationContext)
        this.timeTrackerDao = db.timeTrackerDao()
    }
    suspend fun selectAll(): List<TimeTracker> {
        return withContext(Dispatchers.IO) {
            timeTrackerDao.selectAll()
        }
    }

    fun insert(timeTracker: TimeTracker) {

    }

    inline fun doAsync(todo: () -> Unit) {

        todo()
    }

    inline fun <T> returnAsync(todo: () -> T): T {
        return todo()
    }
}