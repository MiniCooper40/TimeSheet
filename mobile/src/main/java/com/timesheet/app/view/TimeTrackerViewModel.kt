package com.timesheet.app.view

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.timesheet.app.data.dao.TimeTrackerDao
import com.timesheet.app.data.dao.TrackedTimeDao
import com.timesheet.app.data.model.TimeTracker
import com.timesheet.app.data.model.TrackedTime
import com.timesheet.app.ui.Day
import com.timesheet.app.ui.heatmap.CalenderDay
import com.timesheet.app.ui.heatmap.HeatMapDetails
import com.timesheet.app.ui.millisecondsInDay
import com.timesheet.app.view.model.TimeTrackerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

data class WeeklyComparison(
    var timeLastWeek: Long,
    var timeThisWeek: Long,
    val weeklyChartEntryModelProducer: ChartEntryModelProducer
)

class TimeTrackerViewModel(
    var timeTrackerDao: TimeTrackerDao,
    var trackedTimeDao: TrackedTimeDao,
    val uid: Int
): ViewModel() {

    private val _trackedTimes = MutableStateFlow(TimeTrackerUiState())
    val timeTrackers = _trackedTimes.asStateFlow()

    internal val weeklyChartEntryModelProducer = ChartEntryModelProducer()
    internal var weeklyDurations: List<Pair<Int, Duration>> = listOf()


    internal val weeklyComparison = WeeklyComparison(0L,0L,weeklyChartEntryModelProducer)

    private val _monthlyHeatMap = MutableStateFlow(
        HeatMapDetails(listOf())
    )
    internal val monthlyHeatMap = _monthlyHeatMap.asStateFlow()

    init {
        updateState()
        dailyTimesInPastWeek()
    }

    private fun updateMonthlyHeatmap() {
        viewModelScope.launch {

            val currentDay = LocalDate.now()

            val firstDayOfMonth = currentDay.minusDays(currentDay.dayOfMonth.toLong())
            val lastDayOfMonth = currentDay.plusDays((currentDay.lengthOfMonth() - currentDay.dayOfMonth).toLong())

            Log.v("currentDay", currentDay.toString())
            Log.v("firstDayOfMonth", firstDayOfMonth.toString())
            Log.v("lastDayOfMonth", lastDayOfMonth.toString())

            dailyTimesInTimeRange(
                firstDayOfMonth,
                lastDayOfMonth
                ){ durations ->
                Log.v("durations", durations.toString())

                val days: MutableList<MutableList<CalenderDay>> = mutableListOf()

                durations.forEach {
                    val date = firstDayOfMonth.withDayOfMonth(it.first)
                    val dayOfMonth = date.dayOfMonth
                    val weekOfMonth = dayOfMonth/7
                    if(days.size <= weekOfMonth) days.add(mutableListOf())

                    days[weekOfMonth].add(CalenderDay(
                        dayOfMonth = dayOfMonth,
                        duration = it.second
                        )
                    )
                }

                Log.v("days", days.toString())

                val sortedDays = days.map { day ->
                    day.sortedBy { it.dayOfMonth }.toMutableList()
                }


                val firstWeek = sortedDays[0]
                while(firstWeek.size != 7) firstWeek.add(0, CalenderDay(0, Duration.ZERO))

                val lastWeek = sortedDays.last()
                while(lastWeek.size != 7)  lastWeek.add(lastWeek.size, CalenderDay(0, Duration.ZERO))

                val heatMapDetails = HeatMapDetails(sortedDays.map { day ->
                    day.toList()
                })

                Log.v("sortedDays", sortedDays.toString())

                heatMapDetails.elements.forEach {
                    Log.v("heatMapDetails", it.toString())
                }
                _monthlyHeatMap.value = heatMapDetails
            }
        }
    }

    fun updateState() {
        viewModelScope.launch {
            _trackedTimes.value = TimeTrackerUiState(
                timeTrackerDao.getTrackedTimesByUid(uid)
            )

            val currentTime = LocalDate.now().plusDays(5)
            dailyTimesInTimeRange(currentTime.minusDays(14), currentTime.minusDays(7)) { pastWeek ->
                dailyTimesInTimeRange(currentTime.minusDays(6), currentTime.plusDays(1)) { currentWeek ->
                    val currentList = currentWeek.map{ it.second.toMillis() }.toTypedArray()
                    val pastList = pastWeek.map{ it.second.toMillis() }.toTypedArray()

                    weeklyChartEntryModelProducer.setEntries(
                        entriesOf(*pastList), entriesOf(*currentList)
                    )

                    weeklyComparison.timeLastWeek = pastList.sum()
                    weeklyComparison.timeThisWeek = currentList.sum()
                }
            }

            updateMonthlyHeatmap()
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

                val tracker = timeTrackerDao.selectByUid(uid)

                Log.v("TRACKERS FOUND", numTrackers.toString())

                Log.v("tracker sync", tracker.toString())

                if(newStartTime == 0L) {

                    if(!weeklyDurations.isEmpty()) weeklyDurations.last().second.plusMillis(endTime-updatedTracker.startTime)
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
    fun dailyTimesInTimeRange(startDate:LocalDate, endDate: LocalDate, onDetermined: (List<Pair<Int, Duration>>) -> Unit) {

        viewModelScope.launch {
            val durationPairs = mutableListOf<Pair<Int, Duration>>()

            Log.v("start", startDate.toString())
            Log.v("end", endDate.toString())

            runBlocking {

                val trackedTimes = timeTrackerDao.getTrackedTimesByUid(uid)
                val days = Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toDays()
                Log.v("days between", "days between = $days")

                val endDateTime = endDate.atStartOfDay()
                val startDateTime = startDate.atStartOfDay()

                val startOfDayInstant = endDateTime.toInstant(ZoneOffset.UTC)
                var earliestTimeInstant = startDateTime.toInstant(ZoneOffset.UTC)

                val trackedInLastWeek =
                    trackedTimes.trackedTimes.filter { it.endTime > earliestTimeInstant.toEpochMilli() && it.startTime != 0L }
                val zeroStartTimes = trackedTimes.trackedTimes.filter { it.startTime == 0L }

                val times = mutableListOf<Day>()
                for (i in 1..days) {
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

//    Log.v("earliest day", earliestDay.toString())

//    Log.v("pairs", durationPairs.toString())
//                    Thread.sleep(1000L)
//                    Log.v("emited", "emited")

                }
                val durationSum = durations.sumOf { it.toMillis() }
                val timeSum = trackedInLastWeek.sumOf { it.endTime - it.startTime }

//    Log.v("durationSum", durationSum.toString() + ", " + (durationSum/60000).toString())
//    Log.v("timeSum", timeSum.toString() + ", " + (timeSum/60000).toString())

                val durationsString = durations.map { it.toMinutes().toString() }

//    Log.v("DURATIONS",durations.toString())

                var startDay = startDateTime.dayOfWeek.value

                durations.forEach {
//                        durationPairs.add(
//                            (if (startDay > 7) startDay - 7 else startDay) to it
//                        )
//                    val day = if (startDay > 7) startDay - 7 else startDay
                    durationPairs.add(startDay to it)
//                        durationMap.put(
//                            day,
//                            durationMap.get(day)?.plus(it) ?: it
//                        )
                    startDay++
                }
                Log.v("duration pairs", durationPairs.toString())
                onDetermined(durationPairs)
//                weeklyDurations = durationMap.map { it.key to it.value }
//                Log.v("Got weekly durations", weeklyDurations.toString())
            }

        }
    }

    fun dailyTimesInPastWeek() {

        viewModelScope.launch {
            val durationPairs = mutableListOf<Pair<Int, Duration>>()

            runBlocking {

                val trackedTimes = timeTrackerDao.getTrackedTimesByUid(uid)
                val days = 7

                val currentDate = LocalDate.now().atStartOfDay()
                val earliestDay = currentDate.minusDays((days-1).toLong())

                val startOfDayInstant = currentDate.toInstant(ZoneOffset.UTC)
                var earliestTimeInstant = earliestDay.toInstant(ZoneOffset.UTC)

                val trackedInLastWeek =
                    trackedTimes.trackedTimes.filter { it.endTime > earliestTimeInstant.toEpochMilli() && it.startTime != 0L }
                val zeroStartTimes = trackedTimes.trackedTimes.filter { it.startTime == 0L }

                val times = mutableListOf<Day>()
                for (i in 1..days) {
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

//    Log.v("earliest day", earliestDay.toString())

//    Log.v("pairs", durationPairs.toString())
//                    Thread.sleep(1000L)
//                    Log.v("emited", "emited")

                }
                val durationSum = durations.sumOf { it.toMillis() }
                val timeSum = trackedInLastWeek.sumOf { it.endTime - it.startTime }

//    Log.v("durationSum", durationSum.toString() + ", " + (durationSum/60000).toString())
//    Log.v("timeSum", timeSum.toString() + ", " + (timeSum/60000).toString())

                val durationsString = durations.map { it.toMinutes().toString() }

//    Log.v("DURATIONS",durations.toString())

                var startDay = earliestDay.dayOfWeek.value

                durations.forEach {
//                        durationPairs.add(
//                            (if (startDay > 7) startDay - 7 else startDay) to it
//                        )
                    val day = if (startDay > 7) startDay - 7 else startDay
                    durationPairs.add(day to it)
//                        durationMap.put(
//                            day,
//                            durationMap.get(day)?.plus(it) ?: it
//                        )
                    startDay++
                }
                weeklyDurations = durationPairs
//                weeklyDurations = durationMap.map { it.key to it.value }
//                Log.v("Got weekly durations", weeklyDurations.toString())
            }

        }
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