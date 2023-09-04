package com.timesheet.app

import com.timesheet.app.ui.heatmap.chartDataToMonth
import com.timesheet.app.view.data.TimeSheetChartData
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun monthTest() {
        val days = listOf(
            1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16
        ).map { it.toFloat() }
        val startOffset = 4

        val expected = listOf(
            listOf(1,2,3).map { it.toFloat() },
            listOf(4,5,6,7,8,9,10).map { it.toFloat() },
            listOf(11,12,13,14,15,16).map { it.toFloat() }
        )

        val chartData = TimeSheetChartData(days)

        val actual = chartDataToMonth(chartData, startOffset = startOffset)

        println("expected: $expected")
        println("actual: $actual")

        assertTrue(
            actual.equals(expected)
        )
    }
}