package com.timesheet.app.view.model

data class TimeSheetChartData(
    val data: List<Float>,
    val valueFormatter: ChartDataFormatter = defaultValueFormatter,
    val labelFormatter: ChartDataFormatter = defaultValueFormatter
)

fun interface ChartDataFormatter {
    fun format(index: Int, value: Float): String
}

data class HeatMapData(
    val chartData: TimeSheetChartData = TimeSheetChartData(listOf()),
    val offset: Int = 0
)

val defaultValueFormatter = ChartDataFormatter { _: Int, fl: Float -> fl.toString() }