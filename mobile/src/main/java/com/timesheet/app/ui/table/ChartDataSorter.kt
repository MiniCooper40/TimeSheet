package com.timesheet.app.ui.table

import com.timesheet.app.view.data.TimeTrackerChartData

sealed class ChartDataSorter {

    abstract var descending: Boolean
    abstract fun sort(timeTrackerChartData: TimeTrackerChartData): TimeTrackerChartData
}

class NoSort(override var descending: Boolean = true) : ChartDataSorter() {
    override fun sort(timeTrackerChartData: TimeTrackerChartData): TimeTrackerChartData {
        return timeTrackerChartData
    }
}

class DurationSorter(override var descending: Boolean = true) : ChartDataSorter() {
    override fun sort(timeTrackerChartData: TimeTrackerChartData): TimeTrackerChartData {
        return timeTrackerChartData.copy(
            tracked = if(descending) timeTrackerChartData.tracked.sortedByDescending { it.duration.toMillis() }
            else timeTrackerChartData.tracked.sortedBy { it.duration.toMillis() }
        )
    }
}

class SessionSorter(override var descending: Boolean = true) : ChartDataSorter() {
    override fun sort(timeTrackerChartData: TimeTrackerChartData): TimeTrackerChartData {
        return timeTrackerChartData.copy(
            tracked = if(descending) timeTrackerChartData.tracked.sortedByDescending { it.sessions }
            else timeTrackerChartData.tracked.sortedBy { it.sessions }
        )
    }
}

class AlphabeticalSorter(override var descending: Boolean = true) : ChartDataSorter() {
    override fun sort(timeTrackerChartData: TimeTrackerChartData): TimeTrackerChartData {
        return timeTrackerChartData.copy(
            tracked = if(descending) timeTrackerChartData.tracked.sortedByDescending { it.timeTracker.title }
            else timeTrackerChartData.tracked.sortedBy { it.timeTracker.title }
        )
    }
}
