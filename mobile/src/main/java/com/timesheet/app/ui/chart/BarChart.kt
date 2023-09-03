package com.timesheet.app.ui.chart

import android.graphics.Typeface
import android.text.Layout
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
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
import com.timesheet.app.ui.IconDetails
import com.timesheet.app.ui.TimeTrackerStamp
import com.timesheet.app.ui.toCompressedTimeStamp
import com.timesheet.app.view.TimeSpanComparisonChartModel
import com.timesheet.app.view.TimeTrackerComparisonChartModel
import com.timesheet.app.view.TrackedTimeSpan
import java.time.DayOfWeek


fun fillTimeStampZeros(time: Int): String {
    return if (time < 10) "0${time}" else time.toString()
}

fun toTimeStamp(milliseconds: Long?): String {

    if (milliseconds == null) return "0"

    val seconds = (milliseconds / 1000).toInt() % 60
    val minutes = (milliseconds / (1000 * 60) % 60).toInt()
    val hours = (milliseconds / (1000 * 60 * 60) % 24).toInt()

    return "${fillTimeStampZeros(hours)}:${fillTimeStampZeros(minutes)}:${
        fillTimeStampZeros(
            seconds
        )
    }"
}

@Composable
fun Changed(thisWeek: Long, lastWeek: Long) {
    var text = "${toCompressedTimeStamp(thisWeek)} "
    var icon = IconDetails()

    var changed = ""

    if (thisWeek == lastWeek) text = "0s"
    if (thisWeek > lastWeek) {
        changed = toCompressedTimeStamp(thisWeek - lastWeek)
        icon = IconDetails(
            Icons.Default.KeyboardArrowUp, Color.Green
        )
    }
    if (thisWeek < lastWeek) {
        changed = toCompressedTimeStamp(lastWeek - thisWeek)
        icon = IconDetails(
            Icons.Default.KeyboardArrowDown, Color.Red
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 5.dp)
        ) {
            Text("Total: ", style = MaterialTheme.typography.subtitle2)
            Text(text, style = MaterialTheme.typography.subtitle2)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Change: ", style = MaterialTheme.typography.subtitle2)
            Text(changed, style = MaterialTheme.typography.subtitle2)
            Icon(
                icon.icon,
                "Trend indicator",
                tint = icon.tint,
                modifier = Modifier
                    .size(20.dp)
            )
        }
    }
}


@Composable
fun TrackedTimeDailyChart(weeklyComparison: TimeTrackerComparisonChartModel, requestData: (Int) -> Unit) {

    var selected by remember { mutableStateOf(0) }
    val comparisonData by weeklyComparison.comparison.collectAsState()

    Log.v("SELECTED CHART", selected.toString())

    LaunchedEffect(key1 = selected) {
        requestData(selected)
    }

    val chartEntryModelProducer = weeklyComparison.weeklyChartEntryModelProducer

    Log.v("Chart Model", weeklyComparison.toString())

    val recentTimeSpan = comparisonData.trackedTimeSpans.firstOrNull() ?: TrackedTimeSpan(0, "")
    val trackedInRecentTimeSpan = recentTimeSpan.trackedTime

    val previousTimeSpan = comparisonData.trackedTimeSpans.lastOrNull() ?: TrackedTimeSpan(0, "")
    val trackedInPreviousTimeSpan = previousTimeSpan.trackedTime

    val lastDayOfWeek = comparisonData.lastDayOfWeek

    val horizontalValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        DayOfWeek.of((value.toInt() + lastDayOfWeek.value) % 7 + 1).toString()[0].toString()
    }

    val verticalValueFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
        toCompressedTimeStamp(value.toLong())
    }

    val chartStyle = rememberChartStyle()

    Log.v("LABEL1", recentTimeSpan.label)
    Log.v("LABEL2", previousTimeSpan.label)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { selected++ }) {
            Icon(
                Icons.Default.KeyboardArrowLeft,
                "Left"
            )
        }
        Text(recentTimeSpan.label)
        IconButton(onClick = {
            if (selected > 0) selected--
        }) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                "Right"
            )
        }
    }

    TimeSheetTheme {
        Changed(thisWeek = trackedInRecentTimeSpan, lastWeek = trackedInPreviousTimeSpan)
        ProvideChartStyle(chartStyle) {
            val marker = rememberMarker()
            Chart(
                chart = com.patrykandpatrick.vico.compose.chart.column.columnChart(
                    persistentMarkers = remember(
                        marker
                    ) { mapOf(10f to marker) }),
                chartModelProducer = chartEntryModelProducer,
                startAxis = startAxis(
                    valueFormatter = verticalValueFormatter
                ),
                legend = rememberLegend(listOf(
                    previousTimeSpan.label to Grey,
                    recentTimeSpan.label to Black
                )),
                bottomAxis = bottomAxis(
                    valueFormatter = horizontalValueFormatter
                ),
                marker = marker,
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun Stopwatch(timeTracker: TimeTracker, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TimeTrackerStamp(tracker = timeTracker, default = "00:00:00")
        IconButton(
            onClick = onClick
        ) {
            val icon =
                if (timeTracker.startTime == 0L) Icons.Filled.PlayArrow else Icons.Default.Done

            Icon(
                icon,
                "View more for this tracker",
                modifier = Modifier.size(50.dp),
                tint = White
            )
        }
    }
}

@Composable
internal fun rememberChartStyle(): ChartStyle {
    return remember {
        ChartStyle(
            axis = ChartStyle.Axis(
                axisLabelColor = Black,
                axisGuidelineColor = Color.Transparent,
                axisLineColor = Black,
                axisTickWidth = 1.dp,
                axisGuidelineWidth = 1.dp,
                axisLabelTypeface = Typeface.SANS_SERIF,
            ),
            columnChart = ChartStyle.ColumnChart(
                columns = listOf(
                    LineComponent(
                        color = Grey.toArgb(),
                        strokeColor = Grey.toArgb(),
                        shape = Shapes.roundedCornerShape(allPercent = 50),
                        thicknessDp = 6f,
                    ),
                    LineComponent(
                        color = Black.toArgb(),
                        strokeColor = Black.toArgb(),
                        shape = Shapes.roundedCornerShape(allPercent = 50),
                        thicknessDp = 6f,
                    ),
                )
            ),
            lineChart = ChartStyle.LineChart(
                lines = listOf(
                    LineChart.LineSpec(
                        lineColor = Color.Yellow.toArgb()
                    )
                )
            ),
            marker = ChartStyle.Marker(),
            elevationOverlayColor = Color.Black,
        )
    }

}

@Composable
internal fun rememberMarker(): Marker {

    val labelBackgroundColor = Black
    val labelBackgroundShape = MarkerCorneredShape(Corner.FullyRounded)
    val labelBackgroundRadius = 2f
    val labelBackground = remember(labelBackgroundColor) {
        ShapeComponent(labelBackgroundShape, labelBackgroundColor.toArgb()).setShadow(
            radius = labelBackgroundRadius,
            dy = 2f,
            applyElevationOverlay = true,
        )
    }
    val label = textComponent(
        background = labelBackground,
        lineCount = 1,
        padding = dimensionsOf(all = 8.dp),
        typeface = Typeface.SANS_SERIF,
        color = White
    )
    val indicatorInnerComponent = shapeComponent(Shapes.pillShape, White)
    val indicatorCenterComponent = shapeComponent(Shapes.pillShape, White)
    val indicatorOuterComponent = shapeComponent(Shapes.pillShape, White)
    val indicator = overlayingComponent(
        outer = indicatorOuterComponent,
        inner = overlayingComponent(
            outer = indicatorCenterComponent,
            inner = indicatorInnerComponent,
            innerPaddingAll = 4.dp,
        ),
        innerPaddingAll = 4.dp,
    )
    val guideline = lineComponent(
        Black.copy(alpha = 0.3f),
        1.dp,
        Shapes.rectShape,
    )

    return remember(label, indicator, guideline) {
        object : MarkerComponent(label, indicator, guideline) {
            init {
                indicatorSizeDp = 4f
                onApplyEntryColor = { entryColor ->
                    indicatorOuterComponent.color = White.toArgb()
                    with(indicatorCenterComponent) {
                        color = entryColor
                        setShadow(radius = 2f, color = entryColor)
                    }
                }
                labelFormatter =
                    MarkerLabelFormatter { entryModels: List<Marker.EntryModel>, chartValues: ChartValues ->
                        toCompressedTimeStamp(entryModels[0].entry.y.toLong())
                    }
            }


            override fun getInsets(
                context: MeasureContext,
                outInsets: Insets,
                horizontalDimensions: HorizontalDimensions,
            ) = with(context) {
                outInsets.top = label.getHeight(context) + labelBackgroundShape.tickSizeDp.pixels +
                        4f.pixels * 3 -
                        2f.pixels
            }
        }
    }
}

@Composable
fun rememberLegend(items: List<Pair<String, Color>>) = horizontalLegend(
    items = items.mapIndexed { index, details ->
        legendItem(
            icon = shapeComponent(Shapes.pillShape, details.second),
            label = textComponent(
                color = currentChartStyle.axis.axisLabelColor,
                textSize = 12.sp,
                typeface = Typeface.SANS_SERIF,
                textAlignment = Layout.Alignment.ALIGN_CENTER
            ),
            labelText = details.first,
        )
    },
    iconSize = 10.dp,
    iconPadding = 10.dp,
    spacing = 10.dp,
    padding = MutableDimensions(10f, 10f)
)
