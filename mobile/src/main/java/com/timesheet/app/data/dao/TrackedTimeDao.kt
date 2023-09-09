package com.timesheet.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timesheet.app.data.entity.TrackedTime

@Dao
interface TrackedTimeDao {

    @Query("SELECT * FROM tracked_time")
    suspend fun selectAll(): List<TrackedTime>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(trackedTime: TrackedTime)

    @Query("SELECT * FROM tracked_time WHERE end_time >= :startTime AND start_time <= :endTime AND start_time != 0")
    suspend fun trackedTimesInWindow(startTime: Long, endTime: Long): List<TrackedTime>

    @Query("SELECT * FROM tracked_time WHERE end_time >= :startTime AND start_time <= :endTime AND start_time != 0 AND tracker_uid IN (:trackerIds)")
    suspend fun trackedTimesInWindowForIds(startTime: Long, endTime: Long, trackerIds: List<Int>): List<TrackedTime>

}