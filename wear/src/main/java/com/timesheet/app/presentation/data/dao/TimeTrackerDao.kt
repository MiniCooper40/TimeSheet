package com.timesheet.app.presentation.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timesheet.app.presentation.data.model.TimeTracker
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeTrackerDao {
    @Query("SELECT * FROM time_trackers")
    suspend fun selectAll(): List<TimeTracker>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(timeTracker: TimeTracker)

    @Update
    suspend fun update(timeTracker: TimeTracker)
}