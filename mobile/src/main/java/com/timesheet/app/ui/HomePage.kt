package com.timesheet.app.ui

import android.graphics.drawable.Icon
import android.graphics.drawable.shapes.Shape
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.Typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.materialIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.extension.getFieldValue
import com.patrykandpatrick.vico.core.extension.sumByFloat
import com.patrykandpatrick.vico.core.extension.sumOf
import com.timesheet.app.data.model.TimeTracker
import com.timesheet.app.presentation.theme.Cream
import com.timesheet.app.presentation.theme.Grey
import com.timesheet.app.presentation.theme.White
import com.timesheet.app.ui.pie.Legend
import com.timesheet.app.ui.pie.PieChart
import com.timesheet.app.ui.pie.PieChartSlice
import com.timesheet.app.ui.table.Alphabetical
import com.timesheet.app.ui.table.NoSort
import com.timesheet.app.ui.table.Sessions
import com.timesheet.app.ui.table.Table
import com.timesheet.app.ui.table.TableSortType
import com.timesheet.app.view.HomePageViewModel
import com.timesheet.app.view.TimeSheetViewModel
import com.timesheet.app.view.model.TimeTrackerChartData
import com.timesheet.app.view.model.TrackerMetrics
import kotlinx.coroutines.flow.forEach
import java.lang.Math.pow
import java.time.Duration
import java.util.Objects
import java.util.Random
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


@Composable
fun HomePage(navigateTo: (Int) -> Unit) {

    val homePageViewModel: HomePageViewModel = viewModel(factory = HomePageViewModel.Factory)

    val state by homePageViewModel.lastWeekData.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        val rnd = Random()

        val data = state.tracked.map { tracked ->
            PieChartSlice(
                Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)),
                tracked.duration.toMillis(),
                tracked.timeTracker.title,
                uid = tracked.timeTracker.uid
            ) {
                Text(tracked.timeTracker.title)
                Text(
                    toCompressedTimeStamp(tracked.duration.toMillis()),
                    style = MaterialTheme.typography.h3
                )
                Text(
                    "${
                        String.format(
                            "%.2f",
                            tracked.percentage
                        )
                    }%. ${tracked.sessions} session${if (tracked.sessions == 1) "" else "s"}."
                )
            }
        }

        Sections(
            mapOf(
                1 to Section(
                    icon = Icons.Default.DateRange,
                    title = "Table"
                ) {
                    Table(
                        chartData = state,
                        navigateTo = { navigateTo(it) },
                        sortBy = { homePageViewModel.sortWeeklyBy(it) })
                },
                2 to Section(
                    icon = Icons.Default.AccountCircle,
                    title = "Pie Chart"
                ) {
                    PieChart(data)
                    Legend(data.map { it.color to it.title })
                }
            )
        )
    }
}