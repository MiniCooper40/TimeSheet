package com.timesheet.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
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

    @Update
    suspend fun update(trackerGroup: TrackerGroup)

    @Query("DELETE FROM trackergroupitem WHERE group_uid = :groupUid AND tracker_uid IN (:trackerUids) ")
    suspend fun removeTrackersForGroup(groupUid: Int, trackerUids: List<Int>)

    @Query("DELETE FROM trackergroupitem WHERE tracker_uid = :trackerUid ")
    suspend fun removeGroupAssociationsForTracker(trackerUid: Int)

    @Delete
    suspend fun deleteTracker(timeTracker: TimeTracker)

    @Delete
    suspend fun deleteGroup(group: TrackerGroup)

    @Query("DELETE FROM trackergroup WHERE uid = :groupUid")
    suspend fun deleteGroupByUid(groupUid: Int)

    @Query("DELETE FROM trackergroupitem WHERE group_uid = :groupUid ")
    suspend fun removeTrackerAssociationsForGroup(groupUid: Int)

    @Query("DELETE FROM time_tracker WHERE uid = :timeTrackerUid")
    suspend fun deleteTrackerByUid(timeTrackerUid: Int)

    @Query("DELETE FROM tracked_time WHERE tracker_uid = :timeTrackerUid")
    suspend fun deleteTrackedTimeForTrackerUid(timeTrackerUid: Int)

    @Query("SELECT * FROM time_tracker WHERE uid = :uid")
    suspend fun selectByUid(uid: Int): TimeTracker


    @Query("UPDATE time_tracker SET start_time=:startTime WHERE uid=:uid")
    suspend fun updateStartTimeByUid(uid: Int, startTime: Long)

    @Transaction
    @Query("SELECT * FROM time_tracker WHERE uid=:uid")
    suspend fun getTrackedTimesByUid(uid: Int): TrackedTimes?

    @Transaction
    @Query("SELECT * FROM TrackerGroup")
    suspend fun getTrackerGroupsWithMembers(): List<GroupWithTrackers>

    @Transaction
    @Query("SELECT * FROM TrackerGroup WHERE uid = :uid")
    suspend fun getTrackerGroupByGroupUid(uid: Int): GroupWithTrackers

    @Query("SELECT COUNT(*) FROM tracked_time WHERE tracker_uid = :uid")
    suspend fun numberOfTrackersForUid(uid: Int): Int


}