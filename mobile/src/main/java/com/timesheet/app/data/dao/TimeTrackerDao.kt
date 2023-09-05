package com.timesheet.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.timesheet.app.data.entity.GroupWithTrackers
import com.timesheet.app.data.entity.TimeTracker
import com.timesheet.app.data.entity.TrackedTimes
import com.timesheet.app.data.entity.TrackerGroup
import com.timesheet.app.data.entity.TrackerGroupItem

@Dao
interface TimeTrackerDao {

    @Query("SELECT * FROM time_tracker")
    suspend fun selectAll(): List<TimeTracker>

    @Insert
    suspend fun insert(timeTracker: TimeTracker): Long

    @Insert
    suspend fun insert(group: TrackerGroup): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(groupItem: TrackerGroupItem): Long

    @Update
    suspend fun update(timeTracker: TimeTracker)

    @Query("SELECT * FROM time_tracker WHERE uid = :uid")
    suspend fun selectByUid(uid: Int): TimeTracker

    @Query("UPDATE time_tracker SET start_time=:startTime WHERE uid=:uid")
    suspend fun updateStartTimeByUid(uid: Int, startTime: Long)

    @Transaction
    @Query("SELECT * FROM time_tracker WHERE uid=:uid")
    suspend fun getTrackedTimesByUid(uid: Int): TrackedTimes

    @Transaction
    @Query("SELECT * FROM TrackerGroup")
    suspend fun getTrackerGroupsWithMembers(): List<GroupWithTrackers>

    @Query("SELECT COUNT(*) FROM tracked_time WHERE tracker_uid = :uid")
    suspend fun numberOfTrackersForUid(uid: Int): Int


}