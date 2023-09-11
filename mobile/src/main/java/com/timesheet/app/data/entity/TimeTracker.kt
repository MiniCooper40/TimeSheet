package com.timesheet.app.data.entity

import android.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_tracker")
data class TimeTracker(
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "start_time") val startTime: Long = 0L,
    @ColumnInfo(name = "color") val color: Int = Color.BLACK,
    @ColumnInfo(name = "description") val description: String? = null,
    @PrimaryKey(autoGenerate = true) val uid: Int = 0
)