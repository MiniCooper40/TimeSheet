package com.timesheet.app.ui.heatmap

import java.time.DayOfWeek
import java.time.LocalDateTime

data class HeatMapDetails(
    val elements: List<Float>,
    val firstDay: LocalDateTime = LocalDateTime.now()
)