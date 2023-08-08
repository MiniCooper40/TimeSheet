package com.timesheet.app.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.timesheet.app.data.model.TrackedTimes

@Dao
interface TrackedTimesDao {

//    @Transaction
//    @Query("SELECT * FROM tracked_time")
//    suspend fun selectAll(): List<TrackedTimes>



}