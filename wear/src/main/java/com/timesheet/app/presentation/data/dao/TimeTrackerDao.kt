package com.timesheet.app.presentation.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.DeleteTable
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timesheet.app.presentation.data.model.TimeTracker

@Dao
interface TimeTrackerDao {
    @Query("SELECT * FROM time_trackers")
    suspend fun selectAll(): List<TimeTracker>

    @Query("SELECT * FROM time_trackers WHERE uid = :uid")
    suspend fun selectByUid(uid: Int): TimeTracker

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(timeTracker: TimeTracker)

    @Update
    suspend fun update(timeTracker: TimeTracker)

    @Delete
    suspend fun delete(timeTracker: TimeTracker)

    @Query("DELETE FROM time_trackers")
    suspend fun deleteContents() {

    }
}