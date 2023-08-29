package com.timesheet.app.view.model

import com.timesheet.app.data.model.TimeTracker
import java.time.Duration

data class TrackerMetrics(
    val timeTracker: TimeTracker,
    val duration: Duration,
    val percentage: Float,
    val sessions: Int = 0
)