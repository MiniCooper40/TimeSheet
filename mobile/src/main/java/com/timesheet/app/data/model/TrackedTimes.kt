package com.timesheet.app.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

data class TrackedTimes(
    @Embedded val timeTracker: TimeTracker,
    @Relation(
        parentColumn = "uid",
        entityColumn = "tracker_uid"
    )
    val trackedTimes: List<TrackedTime>
)
