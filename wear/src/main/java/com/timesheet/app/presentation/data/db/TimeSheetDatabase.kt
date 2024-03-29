package com.timesheet.app.presentation.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.timesheet.app.presentation.data.dao.TimeTrackerDao
import com.timesheet.app.presentation.data.model.TimeTracker

@Database(entities = arrayOf(TimeTracker::class), version = 13)
public abstract class TimeSheetDatabase: RoomDatabase() {

    abstract fun timeTrackerDao(): TimeTrackerDao

    companion object {
        @Volatile
        private var INSTANCE: TimeSheetDatabase? = null

        fun getDatabase(context: Context): TimeSheetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimeSheetDatabase::class.java,
                    "word_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}