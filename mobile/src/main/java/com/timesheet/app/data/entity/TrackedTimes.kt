package com.timesheet.app.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TrackedTimes(
    @Embedded val timeTracker: TimeTracker,
    @Relation(
        parentColumn = "uid",
        entityColumn = "tracker_uid"
    )
    val trackedTimes: List<TrackedTime>
)
