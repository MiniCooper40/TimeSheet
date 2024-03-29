package com.timesheet.app.presentation.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_trackers")
data class TimeTracker(
    @PrimaryKey val uid: Int,
    @ColumnInfo("startTime") var startTime: Long?,
    @ColumnInfo("title") var title: String
)