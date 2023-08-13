package com.timesheet.app.ui

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.timesheet.R
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.style.ChartStyle
import com.patrykandpatrick.vico.compose.style.LocalChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.patrykandpatrick.vico.core.marker.Marker
import com.timesheet.app.data.model.TrackedTimes
import com.timesheet.app.presentation.theme.Black
import com.timesheet.app.presentation.theme.TimeSheetTheme
import com.timesheet.app.view.TimeSheetViewModel
import com.timesheet.app.view.TimeTrackerViewModel
import com.timesheet.app.view.model.TimeTrackerUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.TemporalField

data class Day(
    val startTime: Long,
    val endTime: Long
)

const val millisecondsInDay: Long = 86400000

fun TrackedTimes.dailyTimesInPastWeek(): List<Pair<Int, Duration>> {
    val currentDate = LocalDate .now().atStartOfDay().plusDays(1)
    val earliestDay = currentDate.minusDays(6L)

    val startOfDayInstant = currentDate.toInstant(ZoneOffset.ofHours(-8))
    var earliestTimeInstant = earliestDay.toInstant(ZoneOffset.ofHours(0))

    Log.v("startOfDay", startOfDayInstant.toString())
    Log.v("earliestTime", earliestTimeInstant.toString())

    val trackedInLastWeek = trackedTimes.filter { it.endTime > earliestTimeInstant.toEpochMilli() && it.startTime != 0L  }
    val zeroStartTimes = trackedTimes.filter { it.startTime == 0L }

    val times = mutableListOf<Day>()
    for(i in 1..7) {
        val day = Day(startTime = earliestTimeInstant.toEpochMilli(), endTime = earliestTimeInstant.toEpochMilli()+millisecondsInDay)
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
        Log.v("startInstant", startInstant.toString())
        Log.v("endInstant", endInstant.toString())

        Log.v("start", start.toString())
        Log.v("end", end.toString())

        times.forEachIndexed { dayIndex, day ->
            val initial = start.coerceAtLeast(day.startTime)
            val final = end.coerceAtMost(day.endTime)

            var dayStartInstant = Instant.ofEpochMilli(day.startTime)
            var dayEndInstant = Instant.ofEpochMilli(day.endTime)
//            Log.v("dayStartInstant", dayStartInstant.toString())
//            Log.v("dayEndInstant", dayEndInstant.toString())

//            Log.v("day start", day.startTime.toString())
//            Log.v("day end", day.endTime.toString())

            if(start >= day.startTime && end <= day.endTime) {
                //Log.v("1", (end-start).toString())
                durations[dayIndex] = durations[dayIndex].plusMillis(end-start)
            }
            else if(start >= day.startTime && start <= day.endTime && end >= day.endTime) {
                //Log.v("2", (end-start).toString())
                durations[dayIndex] = durations[dayIndex].plusMillis(final - start)
            }
            else if(start <= day.startTime && end <= day.endTime && end >= day.startTime) {
//                Log.v("3", (end-day.startTime).toString())
                durations[dayIndex] = durations[dayIndex].plusMillis(end-day.startTime)
            }
            else if(start <= day.startTime && end >= day.endTime) {
                Log.v("4", (day.endTime - day.startTime).toString())
                durations[dayIndex] = durations[dayIndex].plusMillis(day.endTime - day.startTime)
            }
        }
    }

    val durationSum = durations.sumOf { it.toMillis() }
    val timeSum = trackedInLastWeek.sumOf { it.endTime - it.startTime }

    Log.v("durationSum", durationSum.toString() + ", " + (durationSum/60000).toString())
    Log.v("timeSum", timeSum.toString() + ", " + (timeSum/60000).toString())

    val durationsString = durations.map { it.toMinutes().toString() }

//    Log.v("DURATIONS",durations.toString())

    val durationPairs = mutableListOf<Pair<Int, Duration>>()
    var startDay = earliestDay.dayOfWeek.value

//    Log.v("earliest day", earliestDay.toString())

    durations.forEach {
        durationPairs.add(
            (if(startDay > 7) startDay-7 else startDay) to it
        )
        startDay++
    }

//    Log.v("pairs", durationPairs.toString())

    return durationPairs

}


fun fillTimeStampZeros(time: Int): String {
    return if (time < 10) "0${time}" else time.toString()
}

fun toCompressedTimeStamp(milliseconds: Long?): String {

    if (milliseconds == null) return "0"

    val seconds = (milliseconds / 1000).toInt() % 60
    val minutes = (milliseconds / (1000 * 60) % 60).toInt()
    val hours = (milliseconds / (1000 * 60 * 60) % 24).toInt()

    return "${hours}h${minutes}m${seconds}s"
}

fun toTimeStamp(milliseconds: Long?): String {

    if (milliseconds == null) return "0"

    val seconds = (milliseconds / 1000).toInt() % 60
    val minutes = (milliseconds / (1000 * 60) % 60).toInt()
    val hours = (milliseconds / (1000 * 60 * 60) % 24).toInt()

    return "${fillTimeStampZeros(hours)}:${fillTimeStampZeros(minutes)}:${fillTimeStampZeros(seconds)}"
}

@Composable
fun TrackedTimeDailyChart(dailyTimesInPastWeek: List<Pair<Int, Duration>>) {

    val horizontalValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        DayOfWeek.of(dailyTimesInPastWeek.get(value.toInt()).first).toString()[0].toString()
    }

    val verticalValueFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
        toCompressedTimeStamp(value.toLong())
    }

    Log.v("PAST WEEK", dailyTimesInPastWeek.toString())

    val args = (dailyTimesInPastWeek.map{it.second.toMillis()}.toTypedArray())
    val chartEntryModelProducer = ChartEntryModelProducer(entriesOf(*args))

    val chartStyle = remember {
        ChartStyle(
            axis = ChartStyle.Axis(
                axisLabelColor = Black,
                axisGuidelineColor = Color.Transparent,
                axisLineColor = Black,
                axisTickWidth = 1.dp,
                axisGuidelineWidth = 1.dp,
            ),
            columnChart = ChartStyle.ColumnChart(
                columns = listOf(LineComponent(
                    color = Black.toArgb(),
                    strokeColor = Black.toArgb(),
                    shape = Shapes.roundedCornerShape(allPercent = 50),
                    thicknessDp = 6f
                ))
            ),
            lineChart = ChartStyle.LineChart(
                lines = listOf(LineChart.LineSpec(
                    lineColor = Color.Yellow.toArgb()
                ))
            ),
            marker = ChartStyle.Marker(),
            elevationOverlayColor = Color.Black
        )
    }

    TimeSheetTheme {
        ProvideChartStyle(chartStyle)
        {
            Chart(
                chart = columnChart(),
                chartModelProducer = chartEntryModelProducer,
                startAxis = startAxis(
                    valueFormatter = verticalValueFormatter
                ),
                bottomAxis = bottomAxis(
                    valueFormatter = horizontalValueFormatter
                ),
            )
        }
    }
}

@Composable
fun Display(content: @Composable () -> Unit) {
    content()
}

//@Composable
//fun TrackerDetails(uid: Int, context: Context = LocalContext.current) {
//
//
//
//    Log.v("COMPOSED", "TRACKER DETAILS")
//
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier
//                .verticalScroll(rememberScrollState())
//                .padding(all = 12.dp)
//        ) {
//
//            TrackedTimeDailyChart(trackedTimes = )
//        }
//
//}


@Composable
fun TrackerDetails(uid: Int, context: Context = LocalContext.current) {

        val timeTrackerViewModel: TimeTrackerViewModel = viewModel(factory = TimeTrackerViewModel.factoryFor(uid))
        val timeTrackersObj by timeTrackerViewModel.timeTrackers.collectAsState()

        val timeTracker = timeTrackersObj.trackedTimes.timeTracker
        val trackedTimes = timeTrackersObj.trackedTimes

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(all = 12.dp)
        ) {
            Text(timeTracker.title, style = MaterialTheme.typography.h1)
            Row {
                TimeTrackerStamp(tracker = timeTracker, default = "00:00:00")
                IconButton(
                    onClick = { timeTrackerViewModel.updateTrackerStartTime(context, timeTracker) }
                ) {
                    val icon = if(timeTracker.startTime == 0L) Icons.Default.PlayArrow else Icons.Default.Done
                    Icon(icon,
                        "View more for this tracker",
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
            TrackedTimeDailyChart(trackedTimes.dailyTimesInPastWeek())
        }

}