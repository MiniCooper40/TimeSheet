package com.timesheet.app.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timesheet.app.theme.Black
import com.timesheet.app.theme.Grey
import com.timesheet.app.theme.White
import com.timesheet.app.ui.chart.Stopwatch
import com.timesheet.app.ui.chart.TrackedTimeDailyChart
import com.timesheet.app.ui.heatmap.HeatMap
import com.timesheet.app.view.model.TimeTrackerViewModel

data class Day(
    val startTime: Long,
    val endTime: Long
)

const val millisecondsInDay: Long = 86400000


fun fillTimeStampZeros(time: Int): String = if (time < 10) "0${time}" else time.toString()

fun toCompressedTimeStamp(milliseconds: Long?): String {

    if (milliseconds == null) return "0"

    val seconds = (milliseconds / 1000).toInt() % 60
    val minutes = (milliseconds / (1000 * 60) % 60).toInt()
    val hours = (milliseconds / (1000 * 60 * 60) % 24).toInt()

    val stringBuilder = StringBuilder()
    if(hours != 0) stringBuilder.append("${hours}h")
    if(minutes != 0) stringBuilder.append("${minutes}m")
    stringBuilder.append("${seconds}s")

    return stringBuilder.toString()
}

fun toTimeStamp(milliseconds: Long?): String {

    if (milliseconds == null) return "0"

    val seconds = (milliseconds / 1000).toInt() % 60
    val minutes = (milliseconds / (1000 * 60) % 60).toInt()
    val hours = (milliseconds / (1000 * 60 * 60) % 24).toInt()

    return "${fillTimeStampZeros(hours)}:${fillTimeStampZeros(minutes)}:${fillTimeStampZeros(seconds)}"
}

data class IconDetails(
    val icon: ImageVector = Icons.Default.KeyboardArrowRight,
    val tint: Color = Grey
)

@Composable
fun Banner(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(Black),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 20.dp, top = 8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.h2.copy(textAlign = TextAlign.Center), color = White)
            content()
        }
    }
}

@Composable
fun TrackerDetails(uid: Int, editTracker: () -> Unit, context: Context = LocalContext.current) {

        val timeTrackerViewModel: TimeTrackerViewModel = viewModel(factory = TimeTrackerViewModel.factoryFor(uid))
        val timeTrackersObj by timeTrackerViewModel.timeTrackers.collectAsState()

        val timeTracker = timeTrackersObj.trackedTimes.timeTracker

        val heatMapState = timeTrackerViewModel.monthlyHeatMapState

//        val currentHeatMapState by heatMapState.current.collectAsState()
//        val historicalHeatMapFlows = heatMapState.previous

        val scrollState = rememberScrollState()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(scrollState),
        ) {
            Banner(timeTracker.title) {
                Stopwatch(timeTracker = timeTracker) {
                    timeTrackerViewModel.updateTrackerStartTime(context, timeTracker)
                }
                IconButton(onClick = editTracker) {
                    Icon(
                        Icons.Default.Edit,
                        "Edit"
                    )
                }
            }
            Spacer(modifier = Modifier.size(20.dp))
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ){
                Section(title = "Weekly comparison") {
                    TrackedTimeDailyChart(timeTrackerViewModel.chartModel) {
                        timeTrackerViewModel.weeklyComparisonFor(it)
                    }
                }
                Divider()
                Section(title = "Monthly heatmap") {
                    HeatMap(
                        heatMapState = heatMapState
                    ) {
                        timeTrackerViewModel.acquireMonthlyHeatmapDataFor(it)
                    }
                }
            }
        }


}

