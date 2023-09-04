package com.timesheet.app.view.data

import com.timesheet.app.data.entity.TimeTracker
import com.timesheet.app.data.entity.TrackedTime
import com.timesheet.app.data.entity.TrackedTimes

data class TimeTrackerUiState(
    val trackedTimes: TrackedTimes = TrackedTimes(
        TimeTracker("loading"), listOf<TrackedTime>()
    )
)