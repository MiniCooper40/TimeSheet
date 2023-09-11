package com.timesheet.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.timesheet.app.data.dao.TimeTrackerDao
import com.timesheet.app.data.dao.TrackedTimeDao
import com.timesheet.app.data.dao.TrackedTimesDao
import com.timesheet.app.data.entity.TimeTracker
import com.timesheet.app.data.entity.TrackedTime
import com.timesheet.app.data.entity.TrackerGroup
import com.timesheet.app.data.entity.TrackerGroupItem

@Database(entities = [TimeTracker::class, TrackedTime::class, TrackerGroup::class, TrackerGroupItem::class], version=17)
abstract class TimeSheetDatabase: RoomDatabase() {

    abstract fun timeTrackerDao(): TimeTrackerDao
    abstract fun trackedTimeDao(): TrackedTimeDao
    abstract fun trackedTimesDao(): TrackedTimesDao

    companion object {
        @Volatile
        private var INSTANCE: TimeSheetDatabase? = null

        fun getDatabase(context: Context): TimeSheetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimeSheetDatabase::class.java,
                    "time_sheet_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}