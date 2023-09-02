package com.timesheet.app.ui

import android.content.Context
import android.graphics.Typeface
import android.text.Layout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.overlayingComponent
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.legend.horizontalLegend
import com.patrykandpatrick.vico.compose.legend.legendItem
import com.patrykandpatrick.vico.compose.style.ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.cornered.Corner
import com.patrykandpatrick.vico.core.component.shape.cornered.MarkerCorneredShape
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter
import com.timesheet.app.data.model.TimeTracker
import com.timesheet.app.presentation.theme.Black
import com.timesheet.app.presentation.theme.Grey
import com.timesheet.app.presentation.theme.TimeSheetTheme
import com.timesheet.app.presentation.theme.White
import com.timesheet.app.ui.chart.Stopwatch
import com.timesheet.app.ui.chart.TrackedTimeDailyChart
import com.timesheet.app.ui.heatmap.HeatMap
import com.timesheet.app.view.TimeTrackerViewModel
import com.timesheet.app.view.WeeklyComparison
import com.timesheet.app.view.model.TimeSheetChartData
import java.time.DayOfWeek
import java.time.Duration

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
fun TrackerDetails(uid: Int, context: Context = LocalContext.current) {

        val timeTrackerViewModel: TimeTrackerViewModel = viewModel(factory = TimeTrackerViewModel.factoryFor(uid))
        val timeTrackersObj by timeTrackerViewModel.timeTrackers.collectAsState()

        val timeTracker = timeTrackersObj.trackedTimes.timeTracker

        val heatMapDetails by timeTrackerViewModel.monthlyHeatMap.collectAsState()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState()),
        ) {
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
                    Text(timeTracker.title, style = MaterialTheme.typography.h2, color = White)
                    Stopwatch(timeTracker = timeTracker) {
                        timeTrackerViewModel.updateTrackerStartTime(context, timeTracker)
                    }
                }
            }
            Spacer(modifier = Modifier.size(20.dp))
            Column(
                modifier = Modifier.padding(20.dp)
            ){
                Section("Past 7 days") {
                    TrackedTimeDailyChart(timeTrackerViewModel.weeklyComparison)
                }
                Section("Past month") {
                    HeatMap(
                        heatMapData = heatMapDetails
                    )
                }
            }
        }


}

