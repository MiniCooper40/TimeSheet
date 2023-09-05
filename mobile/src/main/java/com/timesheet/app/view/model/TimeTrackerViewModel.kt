package com.timesheet.app.view.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.timesheet.app.data.dao.TimeTrackerDao
import com.timesheet.app.data.dao.TrackedTimeDao
import com.timesheet.app.data.entity.TimeTracker
import com.timesheet.app.data.entity.TrackedTime
import com.timesheet.app.ui.Day
import com.timesheet.app.ui.millisecondsInDay
import com.timesheet.app.ui.toCompressedTimeStamp
import com.timesheet.app.application.MyApplication
import com.timesheet.app.data.repository.TimeSheetPreferencesRepository
import com.timesheet.app.data.repository.WeeklyStrategy
import com.timesheet.app.view.data.HeatMapData
import com.timesheet.app.view.data.MutableHistoricalStateFlow
import com.timesheet.app.view.data.TimeSheetChartData
import com.timesheet.app.view.data.TimeTrackerUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


data class TrackedTimeSpan(
    val trackedTime: Long,
    val label: String
)
data class TimeSpanComparisonChartModel(
    var trackedTimeSpans: List<TrackedTimeSpan>,
    val weeklyChartEntryModelProducer: ChartEntryModelProducer,
    var lastDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
)

data class CachedTimeSpanComparisonChartModel(
    var trackedTimeSpans: List<TrackedTimeSpan>,
    val entries: List<List<FloatEntry>>,
    val lastDayOfWeek: DayOfWeek
)

data class ComparisonData(
    var trackedTimeSpans: List<TrackedTimeSpan> = listOf(),
    val lastDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
)

data class TimeTrackerComparisonChartModel(
    val comparison: StateFlow<ComparisonData>,
    val weeklyChartEntryModelProducer: ChartEntryModelProducer
)

class TimeTrackerViewModel(
    private var timeTrackerDao: TimeTrackerDao,
    private var trackedTimeDao: TrackedTimeDao,
    private val preferencesRepository: TimeSheetPreferencesRepository,
    val uid: Int
): ViewModel() {

    private val _trackedTimes = MutableStateFlow(TimeTrackerUiState())
    val timeTrackers = _trackedTimes.asStateFlow()

    private val weeklyChartEntryModelProducer = ChartEntryModelProducer()
    private var weeklyDurations: List<Pair<Int, Duration>> = listOf()


    internal val weeklyComparison = TimeSpanComparisonChartModel(
        listOf(),
        weeklyChartEntryModelProducer
    )


    private var currentComparisonWeek = 0
    private val _comparisonData = MutableStateFlow(
        ComparisonData()
    )

    private val comparisonData = _comparisonData.asStateFlow()

    public val chartModel = TimeTrackerComparisonChartModel(
        comparisonData,
        weeklyChartEntryModelProducer
    )

//    private val _monthlyHeatMap = MutableStateFlow(
//        HeatMapData()
//    )
//    internal val monthlyHeatMap = _monthlyHeatMap.asStateFlow()

    private val _monthlyHeatMapState = MutableHistoricalStateFlow<HeatMapData>()
    val monthlyHeatMapState = _monthlyHeatMapState.toStateFlow()

    init {
        updateState()
    }

    private fun currentLocalDate(): LocalDate {
        val time = ZonedDateTime.now()
        println("time $time")
        return time.toLocalDate()
    }

    private val monthlyHeatMapCache = mutableMapOf<Int, HeatMapData>()

    fun acquireMonthlyHeatmapDataFor(month: Int): Flow<HeatMapData> {

        return when(month <= 0) {
            true -> flow {

            }
            false -> flow {
                if (!monthlyHeatMapCache.containsKey(month)) {
                    val date = ZonedDateTime.now().minusMonths(month.toLong())
                    val heatMap = monthlyHeatMapForDay(date)
                    monthlyHeatMapCache[month] = heatMap
                }
                monthlyHeatMapCache[month]?.let { emit(it) }
            }
        }
    }

    private fun updateMonthlyHeatmap() {
        viewModelScope.launch {
            monthlyHeatMapForDay(ZonedDateTime.now()).let {
                Log.v("MONTHLY HEATMAP", it.toString())
                _monthlyHeatMapState.value = it
            }
        }
    }

    private suspend fun monthlyHeatMapForDay(day: ZonedDateTime): HeatMapData {
        val currentDay = day.toLocalDate()

        val yearMonthDateFormatter = DateTimeFormatter.ofPattern("YY/MM")

        Log.v("CurrentDay in MHMFD", "$currentDay w/ dayOfMonth ${currentDay.dayOfMonth}")

        val firstDayOfMonth =  day.minusDays(currentDay.dayOfMonth.toLong()-1)
        val lastDayOfMonth = day.plusDays((currentDay.lengthOfMonth() - currentDay.dayOfMonth).toLong())

//        Log.v("currentDay", currentDay.toString())
//        Log.v("firstDayOfMonth", firstDayOfMonth.toString())
//        Log.v("lastDayOfMonth", lastDayOfMonth.toString())
//
//        println("start day of month is ${firstDayOfMonth.dayOfMonth}")

        val heatMap = viewModelScope.async {
            dailyTimesInTimeRange(
                firstDayOfMonth,
                lastDayOfMonth
            ).let { durations ->
                Log.v("durations", durations.toString())
                val firstDay = firstDayOfMonth.dayOfWeek.value


                withContext(Dispatchers.Default) {
                    return@withContext HeatMapData(
                        TimeSheetChartData(
                            durations.map { it.second.toMillis().toFloat() },
                            valueFormatter = { _: Int, value: Float ->
                                toCompressedTimeStamp(value.toLong())
                            },
                            labelFormatter = { dayOfMonth: Int, _: Float ->
                                firstDayOfMonth.plusDays(dayOfMonth.toLong()).dayOfMonth.toString()
                            },
                        ),
                        firstDayOfMonth.dayOfWeek.value,
                        firstDayOfMonth.format(yearMonthDateFormatter)
                    )
                }


//                _monthlyHeatMap.value.chartData.data.forEachIndexed{ index, value ->
//                    println("value ${_monthlyHeatMap.value.chartData.valueFormatter.format(index, value)}")
//                    println("label ${_monthlyHeatMap.value.chartData.labelFormatter.format(index, value)}")
//                }
            }
        }

        return heatMap.await()
    }

    private val cachedWeeklyComparisons: MutableMap<Int, CachedTimeSpanComparisonChartModel> = mutableMapOf()

    fun weeklyComparisonFor(week: Int) {


        viewModelScope.launch {

            val weeklyStrategy = preferencesRepository.getWeeklyStrategy()

            val currentTime = ZonedDateTime.now()

            Log.v("Current time", "$currentTime, week is $week")

            Log.v("WEEKLY STRATEGY", weeklyStrategy.toString())

            val relativeRecentTimeSpanStart = when(weeklyStrategy) {
                WeeklyStrategy.TRAILING -> currentTime.minusWeeks(week.toLong()).minusDays(6L)
                WeeklyStrategy.CYCLE -> {
                    val cycleStartDay = preferencesRepository.getWeeklyCycleStartDay()
                    Log.v("CYCLE START DAY", cycleStartDay.toString())
                    val currentStartDay = currentTime.dayOfWeek.value
                    Log.v("CURRENT START DAY", currentTime.dayOfWeek.toString())

                    val diff = currentStartDay - cycleStartDay.value
                    val daysToAdd =
                        if(diff < 0) diff + 7
                        else diff
                    currentTime.minusWeeks(week.toLong()).minusDays((daysToAdd).toLong())
                }
            }

            Log.v("REL REC START", relativeRecentTimeSpanStart.toString())

            val previousTimeSpanStart = relativeRecentTimeSpanStart.minusDays(7)
            val previousTimeSpanEnd = relativeRecentTimeSpanStart.minusDays(1)

            val recentTimeSpanStart = relativeRecentTimeSpanStart
            val recentTimeSpanEnd = relativeRecentTimeSpanStart.plusDays(6L)

            dailyTimesInTimeRange(previousTimeSpanStart, previousTimeSpanEnd).let { pastWeek ->
                dailyTimesInTimeRange(recentTimeSpanStart, recentTimeSpanEnd).let { currentWeek ->
                    val currentList = currentWeek.map{ it.second.toMillis() }.toTypedArray()
                    val pastList = pastWeek.map{ it.second.toMillis() }.toTypedArray()
                    val timeFormatter = DateTimeFormatter.ofPattern("MM/dd")

                    val chartModel = CachedTimeSpanComparisonChartModel(
                        entries = listOf(entriesOf(*pastList), entriesOf(*currentList)),
                        lastDayOfWeek = recentTimeSpanEnd.dayOfWeek,
                        trackedTimeSpans = listOf(
                            TrackedTimeSpan(
                                currentList.sum(),
                                "${recentTimeSpanStart.format(timeFormatter)} - ${recentTimeSpanEnd.format(timeFormatter)}"
                            ),
                            TrackedTimeSpan(
                                pastList.sum(),
                                "${previousTimeSpanStart.format(timeFormatter)} - ${previousTimeSpanEnd.format(timeFormatter)}"
                            ),
                        )
                    )

                    setWeeklyChartModel(chartModel)
                }
            }
        }
    }

    private fun setWeeklyChartModel(model: CachedTimeSpanComparisonChartModel) {
        val entries = model.entries
        val lastDayOfWeek = model.lastDayOfWeek
        val trackedTimeSpans = model.trackedTimeSpans

        weeklyComparison.weeklyChartEntryModelProducer.setEntries(entries)
        weeklyComparison.trackedTimeSpans = trackedTimeSpans
        weeklyComparison.lastDayOfWeek = lastDayOfWeek

        _comparisonData.value = ComparisonData(
            trackedTimeSpans,
            lastDayOfWeek
        )
    }

    private fun updateState() {
        viewModelScope.launch {
            _trackedTimes.value = TimeTrackerUiState(
                timeTrackerDao.getTrackedTimesByUid(uid)
            )

            //val currentTime = currentLocalDate()
//            val currentTime = ZonedDateTime.now()
//            dailyTimesInTimeRange(currentTime.minusDays(13), currentTime.minusDays(7)).let { pastWeek ->
//                dailyTimesInTimeRange(currentTime.minusDays(6), currentTime).let { currentWeek ->
//                    val currentList = currentWeek.map{ it.second.toMillis() }.toTypedArray()
//                    val pastList = pastWeek.map{ it.second.toMillis() }.toTypedArray()
//
//                    weeklyChartEntryModelProducer.setEntries(
//                        entriesOf(*pastList), entriesOf(*currentList)
//                    )
//
//                    weeklyComparison.timeLastWeek = pastList.sum()
//                    weeklyComparison.timeThisWeek = currentList.sum()
//                    weeklyComparison.lastDayOfWeek = currentTime.dayOfWeek
//                }
//            }
            if(currentComparisonWeek == 0) weeklyComparisonFor(currentComparisonWeek)

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
    private suspend fun dailyTimesInTimeRange(startDate: ZonedDateTime, endDate: ZonedDateTime): MutableList<Pair<Int, Duration>> {

        val dailyTimes = viewModelScope.async {
            val durationPairs = mutableListOf<Pair<Int, Duration>>()

            Log.v("start", startDate.toString())
            Log.v("end", endDate.toString())

            runBlocking {

                val trackedTimes = timeTrackerDao.getTrackedTimesByUid(uid)

                val endDateTime = endDate.plusDays(1).withHour(0)
                val startDateTime = startDate.withHour(0)

                val days = Duration.between(startDateTime, endDateTime).toDays()
                Log.v("days between", "days between = $days")

                var earliestTimeInstant = startDateTime.toInstant()

                //println("earliest time instant $earliestTimeInstant")

                val trackedInLastWeek =
                    trackedTimes.trackedTimes.filter { it.endTime > earliestTimeInstant.toEpochMilli() && it.startTime != 0L }

                val times = mutableListOf<Day>()
                for (i in 1..days) {
                    val day = Day(
                        startTime = earliestTimeInstant.toEpochMilli(),
                        endTime = earliestTimeInstant.toEpochMilli() + millisecondsInDay
                    )
                    times.add(day)
                    earliestTimeInstant = earliestTimeInstant.plusMillis(millisecondsInDay)
                }

//                times.forEach {
//                    println("start" + Instant.ofEpochMilli(it.startTime))
//                    println("end" + Instant.ofEpochMilli(it.endTime))
//                }

                val durations = times.map { Duration.ZERO }.toMutableList()

                trackedInLastWeek.forEachIndexed { _, tracked ->
                    val start = tracked.startTime
                    val end = tracked.endTime

                    times.forEachIndexed { dayIndex, day ->
                        val final = end.coerceAtMost(day.endTime)

                        if (start >= day.startTime && end <= day.endTime) {
                            durations[dayIndex] = durations[dayIndex].plusMillis(end - start)
                        } else if (start >= day.startTime && start <= day.endTime) {
                            durations[dayIndex] = durations[dayIndex].plusMillis(final - start)
                        } else if (start <= day.startTime && end <= day.endTime && end >= day.startTime) {
                            durations[dayIndex] = durations[dayIndex].plusMillis(end - day.startTime)
                        } else if (start <= day.startTime && end >= day.endTime) {
                            durations[dayIndex] = durations[dayIndex].plusMillis(day.endTime - day.startTime)
                        }
                    }

                }

                var startDay = startDateTime.dayOfWeek.value

                durations.forEach {
                    durationPairs.add(startDay to it)
                    startDay++
                }

                withContext(Dispatchers.Default) {
                    return@withContext durationPairs
                }
            }
        }

        return dailyTimes.await()
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
                        TimeSheetPreferencesRepository(
                            application.applicationContext
                        ),
                        uid
                    ) as T
                }
            }
            return factory
        }
    }

}