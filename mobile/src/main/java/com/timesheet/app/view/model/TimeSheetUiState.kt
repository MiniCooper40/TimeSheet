package com.timesheet.app.view.model

import com.timesheet.app.data.model.TimeTracker

data class TimeSheetUiState(
    val trackers: List<TimeTracker>
)