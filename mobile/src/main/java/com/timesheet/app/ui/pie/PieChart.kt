package com.timesheet.app.ui.pie

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import kotlin.math.atan2

@OptIn(ExperimentalTextApi::class)
@Composable
fun PieChart(originalData: List<PieChartSlice>) {

    val data = originalData
    val dataSum: Long = data.map { it.data }.sum()

    var chartSize by remember { mutableStateOf(0f) }
    var selected: PieChartSlice? by remember { mutableStateOf(null) }

    fun Long.asAngle(): Float = this / dataSum.toFloat() * 360f

    fun updateSelectedPieSlice(angle: Float) {

        Log.v("START ANGLE", angle.toString())
        data.forEach { Log.v("DATA", it.toString()) }
        Log.v("DATA VALUES", data.size.toString())

        var currentSum = 0L
        data.asReversed().forEach {
            currentSum += it.data
            if (currentSum.asAngle() >= angle) {
                selected = if ((selected?.uid ?: -1) == it.uid) null else it
                return
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { chartSize.toDp() })
                .pointerInput(data) {
                    detectTapGestures {
                        Log.v("CLick", "click at $it")
                        val width = size.width
                        val radius = width / 2
                        val strokeWidth = 20.dp.toPx()
                        val canvasCenter = size.center


                        val strokeEnd = radius
                        val strokeStart = radius - strokeWidth

                        val clickDistance = it - canvasCenter.toOffset()

                        Log.v("Click distance", clickDistance.toString())

                        val distance = clickDistance.getDistance()
                        val circleCoverage = (1.5 * Math.PI - atan2(
                            clickDistance.y,
                            clickDistance.x
                        ) / (2 * Math.PI) * 360f)

                        if (distance < strokeEnd && distance > strokeStart) {
                            Log.v("Click", "Valid click")
                            updateSelectedPieSlice(
                                if (circleCoverage < 0) circleCoverage.toFloat() + 360f else circleCoverage.toFloat()
                            )
                        }
                    }
                }
        ) {
            val width = size.width
            chartSize = width
            val strokeWidth = 20.dp.toPx()

            var startAngle = 0f

            for (index in 0..data.lastIndex) {
                val chartData = data[index]
                val sweepAngle = chartData.data.asAngle()

                val adjustedStrokeWidth =
                    if (chartData.uid == (selected?.uid ?: -1)) strokeWidth / 2 else strokeWidth

                drawArc(
                    color = chartData.color,
                    startAngle = startAngle + 9,
                    sweepAngle = sweepAngle - 9,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(width - strokeWidth, width - strokeWidth),
                    style = Stroke(adjustedStrokeWidth, cap = StrokeCap.Round),
                )


                startAngle += sweepAngle
            }
        }
        selected?.let {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                it.content()
            }
        }
    }

    data.forEach { Log.v("DATArrr", it.toString()) }
}

data class PieChartSlice(
    val color: Color,
    val data: Long,
    val title: String,
    val uid: Int,
    val content: @Composable () -> Unit
)

@Composable
fun Legend(items: List<Pair<Color, String>>) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        items.map {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Canvas(
                    modifier = Modifier.size(10.dp)
                ) {
                    drawCircle(it.first)
                }
                Text(it.second)
            }
        }
    }
}