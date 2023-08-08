package com.timesheet.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_tracker")
data class TimeTracker(
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "startTime") val startTime: Long = 0L,
    @PrimaryKey(autoGenerate = true) val uid: Int = 0
)