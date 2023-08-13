package com.timesheet.app.view

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.timesheet.app.data.dao.TimeTrackerDao
import com.timesheet.app.data.dao.TrackedTimeDao
import com.timesheet.app.data.dao.TrackedTimesDao
import com.timesheet.app.data.model.TimeTracker
import com.timesheet.app.data.model.TrackedTime
import com.timesheet.app.data.model.TrackedTimes
import com.timesheet.app.ui.Day
import com.timesheet.app.ui.millisecondsInDay
import com.timesheet.app.view.model.TimeTrackerUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class TimeTrackerViewModel(
    val timeTrackerDao: TimeTrackerDao,
    val trackedTimeDao: TrackedTimeDao,
    val uid: Int
): ViewModel() {

    private val _trackedTimes = MutableStateFlow(TimeTrackerUiState())
    val timeTrackers = _trackedTimes.asStateFlow()


    init {
        updateState()
    }

    fun updateState() {
        viewModelScope.launch {
            _trackedTimes.value = TimeTrackerUiState(
                timeTrackerDao.getTrackedTimesByUid(uid)
            )
        }
    }

    fun updateTrackerStartTime(context: Context, updatedTracker: TimeTracker) {
        Log.v("tracker", updatedTracker.toString())

        val currentTime = System.currentTimeMillis()
        var newStartTime = if(updatedTracker.startTime == 0L) currentTime else 0L // Start / End
        val endTime = currentTime
        val uid = updatedTracker.uid

        viewModelScope.launch {
            runBlocking {
                val numTrackers = timeTrackerDao.numberOfTrackersForUid(uid)
//                if(numTrackers == 0 && newStartTime == 0L) newStartTime = System.currentTimeMillis()

                val tracker = timeTrackerDao.selectByUid(uid)

                val trackers = timeTrackerDao.getTrackedTimesByUid(tracker.uid)

                Log.v("TRACKERS FOUND", numTrackers.toString())

                Log.v("tracker sync", tracker.toString())

                if(newStartTime == 0L) {
                    val trackedTime = TrackedTime(
                        startTime = tracker.startTime,
                        endTime = endTime,
                        trackerUid = tracker.uid
                    )
                    Log.v("Inserting", trackedTime.toString())
                    trackedTimeDao.insert(trackedTime)
                }

                timeTrackerDao.update(tracker.copy(startTime=newStartTime))

                updateState()

            }
        }
    }

    fun dailyTimesInPastWeek(): MutableStateFlow<List<Pair<Int, Duration>>> {

        val stateFlow = MutableStateFlow(listOf<Pair<Int, Duration>>(
            1 to Duration.ZERO, 2 to Duration.ZERO, 3 to Duration.ZERO, 4 to Duration.ZERO,
            5 to Duration.ZERO, 6 to Duration.ZERO, 7 to Duration.ZERO))

        viewModelScope.launch {
            runBlocking {

                val trackedTimes = timeTrackers.value.trackedTimes.trackedTimes

                val currentDate = LocalDate.now().atStartOfDay()
                val earliestDay = currentDate.minusDays(6L)

                val startOfDayInstant = currentDate.toInstant(ZoneOffset.UTC)
                var earliestTimeInstant = earliestDay.toInstant(ZoneOffset.UTC)

                val trackedInLastWeek =
                    trackedTimes.filter { it.endTime > earliestTimeInstant.toEpochMilli() && it.startTime != 0L }
                val zeroStartTimes = trackedTimes.filter { it.startTime == 0L }

                val times = mutableListOf<Day>()
                for (i in 1..7) {
                    val day = Day(
                        startTime = earliestTimeInstant.toEpochMilli(),
                        endTime = earliestTimeInstant.toEpochMilli() + millisecondsInDay
                    )
                    times.add(day)
                    earliestTimeInstant = earliestTimeInstant.plusMillis(millisecondsInDay)
                }

//    Log.v("times", times.toString())
//    Log.v("zeroStartTimes", zeroStartTimes.toString())

                val durations = times.map { Duration.ZERO }.toMutableList()

                trackedInLastWeek.forEachIndexed { _, tracked ->
                    var start = tracked.startTime
                    var end = tracked.endTime

                    var startInstant = Instant.ofEpochMilli(start)
                    var endInstant = Instant.ofEpochMilli(end)
//        Log.v("startInstant", startInstant.toString())
//        Log.v("endInstant", endInstant.toString())

//        Log.v("start", start.toString())
//        Log.v("end", end.toString())

                    times.forEachIndexed { dayIndex, day ->
                        val initial = start.coerceAtLeast(day.startTime)
                        val final = end.coerceAtMost(day.endTime)

                        var dayStartInstant = Instant.ofEpochMilli(day.startTime)
                        var dayEndInstant = Instant.ofEpochMilli(day.endTime)
//            Log.v("dayStartInstant", dayStartInstant.toString())
//            Log.v("dayEndInstant", dayEndInstant.toString())

//            Log.v("day start", day.startTime.toString())
//            Log.v("day end", day.endTime.toString())

                        if (start >= day.startTime && end <= day.endTime) {
                            //Log.v("1", (end-start).toString())
                            durations[dayIndex] = durations[dayIndex].plusMillis(end - start)
                        } else if (start >= day.startTime && start <= day.endTime && end >= day.endTime) {
                            //Log.v("2", (end-start).toString())
                            durations[dayIndex] = durations[dayIndex].plusMillis(final - start)
                        } else if (start <= day.startTime && end <= day.endTime && end >= day.startTime) {
//                Log.v("3", (end-day.startTime).toString())
                            durations[dayIndex] = durations[dayIndex].plusMillis(end - day.startTime)
                        } else if (start <= day.startTime && end >= day.endTime) {
                            Log.v("4", (day.endTime - day.startTime).toString())
                            durations[dayIndex] =
                                durations[dayIndex].plusMillis(day.endTime - day.startTime)
                            //            }
                        }
                    }

                    val durationSum = durations.sumOf { it.toMillis() }
                    val timeSum = trackedInLastWeek.sumOf { it.endTime - it.startTime }

//    Log.v("durationSum", durationSum.toString() + ", " + (durationSum/60000).toString())
//    Log.v("timeSum", timeSum.toString() + ", " + (timeSum/60000).toString())

                    val durationsString = durations.map { it.toMinutes().toString() }

//    Log.v("DURATIONS",durations.toString())

                    val durationPairs = mutableListOf<Pair<Int, Duration>>()
                    var startDay = earliestDay.dayOfWeek.value

//    Log.v("earliest day", earliestDay.toString())

                    durations.forEach {
                        durationPairs.add(
                            (if (startDay > 7) startDay - 7 else startDay) to it
                        )
                        startDay++
                    }

//    Log.v("pairs", durationPairs.toString())
//                    Thread.sleep(1000L)
//                    Log.v("emited", "emited")
                    stateFlow.value = durationPairs
                }
            }

        }
        return stateFlow
    }
    companion object {

        fun factoryFor(uid: Int): ViewModelProvider.Factory {
            val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    // Get the Application object from extras
                    val application = checkNotNull(extras[APPLICATION_KEY])
                    // Create a SavedStateHandle for this ViewModel from extras
                    val savedStateHandle = extras.createSavedStateHandle()

                    return TimeTrackerViewModel(
                        (application as MyApplication).timeTrackerDao,
                        (application as MyApplication).trackedTimeDao,
                        uid
                    ) as T
                }
            }
            return factory
        }
    }

}