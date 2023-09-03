package com.timesheet.app.ui.heatmap

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.PlainTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.patrykandpatrick.vico.core.extension.copyColor
import com.timesheet.app.presentation.theme.Black
import com.timesheet.app.presentation.theme.White
import com.timesheet.app.ui.EvenlySpacedRow
import com.timesheet.app.ui.toCompressedTimeStamp
import com.timesheet.app.view.model.ChartDataFormatter
import com.timesheet.app.view.model.HeatMapData
import com.timesheet.app.view.model.HistoricalStateFlow
import com.timesheet.app.view.model.TimeSheetChartData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.util.Arrays


fun chartDataToMonth(
    chartData: TimeSheetChartData,
    startOffset: Int = 0,
    columns: Int = 7
): List<List<Float>> {
    val durations = chartData.data

    val month: MutableList<List<Float>> = mutableListOf()

    var endSlice = columns - 1 - startOffset
    var startSlice = 0
    while (startSlice <= durations.lastIndex) {
        month.add(
            durations.slice(startSlice..endSlice)
        )
        println(startSlice)
        println(endSlice)

        startSlice = endSlice + 1
        endSlice = (endSlice + columns).coerceAtMost(durations.lastIndex)
    }

    return month.toList()
}

@Composable
fun HeatMapCell(modifier: Modifier = Modifier, label: String? = null, backgroundColor: Color) {
    Card(
        modifier = modifier,
        elevation = 0.dp,
        contentColor = Black,
        border = BorderStroke(1.dp, SolidColor(Black)),//BorderStroke(width = 1.dp, color = Black),
        backgroundColor = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            label?.let {
                Text(
                    it,
                    color = Black,
                    modifier = Modifier
                        .padding(all = 0.dp),

                    )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatMap(
    modifier: Modifier = Modifier,
    heatMapState: HistoricalStateFlow<HeatMapData>,
    cellWidth: Dp = 45.dp,
    columns: Int = 7,
    retrieveData: (Int) -> Flow<HeatMapData>
) {

    var selected by remember { mutableStateOf(1) }

    Log.v("SELECTED", selected.toString())

    val presentMonthHeatMapData by heatMapState.current.collectAsState()
    val previousMonthHeatMapData by retrieveData(selected).collectAsState(initial = HeatMapData())

//    val current by
//        if(selected == 0) heatMapState.current.collectAsState()
//        else retrieveData(selected).collectAsState(initial = HeatMapData())

    val current = if(selected > 0) previousMonthHeatMapData else presentMonthHeatMapData

//    Log.v("CURRENT", current.toString())

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { selected++ }) {
            Icon(
                Icons.Default.KeyboardArrowLeft,
                "Left"
            )
        }

        current?.label?.let { Text(it) }

        IconButton(onClick = {
            if(selected > 0) selected--
        }) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                "Right"
            )
        }
    }

    current?.let { HeatMapGrid(heatMapData = it) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatMapGrid(
    modifier: Modifier = Modifier,
    heatMapData: HeatMapData,
    cellWidth: Dp = 45.dp,
    columns: Int = 7
) {
    val timeSheetChartData = heatMapData.chartData
    val offset = heatMapData.offset
    val valueFormatter = timeSheetChartData.valueFormatter
    val labelFormatter = timeSheetChartData.labelFormatter

    val daysOfWeek = listOf(
        "S", "M", "T", "W", "T", "F", "S"
    )

    val data = chartDataToMonth(
        timeSheetChartData,
        offset,
        columns = columns
    )

    val scope = rememberCoroutineScope()

    val maxValue = (data.flatten().maxOrNull() ?: 1f).coerceAtLeast(Float.MIN_VALUE)

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        var dayCounter = 0

        data.firstOrNull()?.let {
            EvenlySpacedRow {
                daysOfWeek.map {
                    Box(
                        modifier = Modifier.width(cellWidth),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier
                        )
                    }
                }
            }
        }

        data.mapIndexed { weekOfMonth, week ->

            EvenlySpacedRow {
                if (weekOfMonth == 0) {
                    (1..7 - week.size).map {
                        HeatMapCell(
                            backgroundColor = Color.White,
                            modifier = Modifier
                                .width(cellWidth)
                        )
                    }
                }
                week.map { day ->
                    val dayOfMonth = dayCounter++
                    val label = labelFormatter.format(dayOfMonth, day)
                    val value = valueFormatter.format(dayOfMonth, day)
                    //println("day of month is $dayOfMonth")

                    val toolTipState = remember { PlainTooltipState() }
                    PlainTooltipBox(
                        tooltip = {
                            Text(value, color = White)
                        },
                        tooltipState = toolTipState,
                        shape = RoundedCornerShape(20.dp)
                    ) {

                        val colorRatio = day / maxValue
                        //println("color ration $colorRatio")
                        HeatMapCell(
                            modifier = Modifier
                                .size(45.dp)
                                .clickable { scope.launch { toolTipState.show() } },
                            backgroundColor = Color(
                                ColorUtils.blendARGB(
                                    Color.White.toArgb(),
                                    Color.DarkGray.toArgb(),
                                    colorRatio
                                ).copyColor(alpha = 0.4f)
                            ),
                            label = label,
                        )
                    }
                }
                if (weekOfMonth == data.lastIndex) {
                    (1..7 - week.size).map {
                        HeatMapCell(
                            backgroundColor = Color.White,
                            modifier = Modifier
                                .width(cellWidth)
                        )
                    }
                }
            }
        }
    }
}