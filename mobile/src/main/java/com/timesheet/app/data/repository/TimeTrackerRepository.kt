package com.timesheet.app.data.repository

import android.util.Log
import com.timesheet.app.data.dao.TimeTrackerDao
import com.timesheet.app.data.dao.TrackedTimeDao
import com.timesheet.app.view.data.TimeTrackerChartData
import com.timesheet.app.view.data.TrackerMetrics
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class TimeTrackerRepository(
    private val timeTrackerDao: TimeTrackerDao,
    private val trackedTimeDao: TrackedTimeDao
) {

    suspend fun timeTrackedBetween(startDate: LocalDate, endDate: LocalDate): TimeTrackerChartData {
        val startTimeMillis = startDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val endTimeMillis = endDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

        val trackedTimes = trackedTimeDao.trackedTimesInWindow(startTimeMillis, endTimeMillis)

        val trackedDurations: MutableMap<Int, Duration> = mutableMapOf()

        trackedTimes.forEach { trackedTime ->

            val trackerUid = trackedTime.trackerUid

            if(!trackedDurations.containsKey(trackerUid)) trackedDurations += trackerUid to Duration.ZERO
            val endMillis = trackedTime.endTime.coerceAtMost(endTimeMillis)
            val startMillis = trackedTime.startTime.coerceAtLeast(startTimeMillis)

            val millisToAdd = endMillis - startMillis

            Log.v("MILLIS", "$trackerUid ->$millisToAdd.  ${Instant.ofEpochMilli(startMillis)} to ${Instant.ofEpochMilli(endMillis)}")

//            trackedDurations[trackerUid] = trackedDurations[trackerUid]?.plusMillis(millisToAdd)
            val tracked = trackedDurations[trackerUid]
            tracked?.let {
                trackedDurations[trackerUid] = tracked.plusMillis(millisToAdd)
            }

        }

        Log.v("Durations", trackedDurations.size.toString())

        trackedDurations.forEach{
            Log.v("TRACKED", it.toString())
        }

        val tracked = trackedDurations.mapKeys { timeTrackerDao.selectByUid(it.key) }

        val totalTimeTracked = tracked.toList().sumOf { it.second.toMillis() }

        return TimeTrackerChartData(
            tracked.map { track ->
                TrackerMetrics(
                    timeTracker = track.key,
                    percentage = track.value.toMillis() / totalTimeTracked.toFloat() * 100,
                    duration = track.value,
                    sessions = trackedTimes.count { it.trackerUid == track.key.uid }
                )
            }
                .filter { it.duration.toMillis() > 60000 }
        )
    }
}