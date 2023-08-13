package com.timesheet.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timesheet.app.data.model.TrackedTime

@Dao
interface TrackedTimeDao {

    @Query("SELECT * FROM tracked_time")
    suspend fun selectAll(): List<TrackedTime>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(trackedTime: TrackedTime)

}