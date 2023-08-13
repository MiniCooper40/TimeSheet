package com.timesheet.app.ui.chart

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.views.chart.column.columnChart


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
fun BarChart(data: List<Long>) {

    val chartEntryModel = entryModelOf(*data.toTypedArray())

    val formatter = AxisValueFormatter<AxisPosition.Vertical.Start> { fl: Float, chartValues: ChartValues -> toTimeStamp(fl.toLong())}

    val chart = columnChart(LocalContext.current)
    chart.innerSpacingDp = 1F
    chart.spacingDp = 1.5F
    chart.columns.forEach {
        it.thicknessDp = 1f
    }

    Chart(
        chart = chart,
        model = chartEntryModel,
        startAxis = startAxis(valueFormatter = formatter),
        bottomAxis = bottomAxis(),
        modifier = Modifier
            .width(200.dp)
            .height(100.dp)
    )

}