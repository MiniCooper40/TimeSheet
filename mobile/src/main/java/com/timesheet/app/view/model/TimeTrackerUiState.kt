package com.timesheet.app.view.model

import com.timesheet.app.data.model.TimeTracker
import com.timesheet.app.data.model.TrackedTime
import com.timesheet.app.data.model.TrackedTimes

data class TimeTrackerUiState(
    val trackedTimes: TrackedTimes = TrackedTimes(
        TimeTracker("loading"), listOf<TrackedTime>()
    )
)