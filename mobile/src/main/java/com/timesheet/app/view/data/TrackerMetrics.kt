package com.timesheet.app.view.data

import com.timesheet.app.data.entity.TimeTracker
import java.time.Duration

data class TrackerMetrics(
    val timeTracker: TimeTracker,
    val duration: Duration,
    val percentage: Float,
    val sessions: Int = 0
)