package com.timesheet.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity("tracked_time")
data class TrackedTime(
    @ColumnInfo(name="start_time") val startTime: Long,
    @ColumnInfo(name="end_time") val endTime: Long,
    @ColumnInfo(name="tracker_uid") val trackerUid: Int,
    @PrimaryKey(autoGenerate = true) val uid: Int = 0
)