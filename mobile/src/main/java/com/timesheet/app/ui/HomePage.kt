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
import com.timesheet.app.ui.table.Alphabetical
import com.timesheet.app.ui.table.NoSort
import com.timesheet.app.ui.table.Sessions
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


@OptIn(ExperimentalTextApi::class)
@Composable
fun HomePage(navigateTo: (Int) -> Unit) {

    val homePageViewModel: HomePageViewModel = viewModel(factory = HomePageViewModel.Factory)

    val state by homePageViewModel.lastWeekData.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
//        Table(weeklyData = state, navigateTo = { navigateTo(it) }, sortBy = { homePageViewModel.sortWeeklyBy(it) })
        val rnd = Random()


        val data = state.tracked.mapIndexed { index, tracked ->
            PieChartSlice(Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)), tracked.duration.toMillis(), tracked.timeTracker.title, uid = tracked.timeTracker.uid) {
                Text(tracked.timeTracker.title)
                Text(toCompressedTimeStamp(tracked.duration.toMillis()), style = MaterialTheme.typography.h3)
                Text("${String.format("%.2f", tracked.percentage)}%. ${tracked.sessions} session${if(tracked.sessions == 1) "" else "s"}.")
            }
        }
        data.forEach { Log.v("DATA in Home", it.toString()) }
        Log.v("HOME", "IN HOME")
//        PieChart(data)
//        Legend(data.map{ it.color to it.title})

        Sections(
            mapOf(
                1 to Section(
                    icon = Icons.Default.DateRange,
                    title = "Table"
                ) {
                    Table(weeklyData = state, navigateTo = { navigateTo(it) }, sortBy = { homePageViewModel.sortWeeklyBy(it) })
                },
                2 to Section(
                    icon = Icons.Default.AccountCircle,
                    title = "Pie Chart"
                ) {
                    PieChart(data)
                    Legend(data.map{ it.color to it.title})
                }
            )
        )
    }
}

@Composable
fun ChartTitle(title: String, onClick: () -> Unit) {
//    Surface(
//        shape = RoundedCornerShape(25)
//    ) {
        Text(
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .clickable { onClick() },
//                .padding(4.dp),
            textAlign = TextAlign.Center,
            text = title
        )
//    }
}

@Composable
fun TableCell(text: Any) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
    ) {
        Text(text.toString())
    }
}

@Composable
fun TableRow(trackedMetrics: TrackerMetrics, modifier: Modifier = Modifier, textStyle: TextStyle, readyToDraw: Boolean, setTextStyle: (TextStyle) -> Unit, setReadyToDraw: (Boolean) -> Unit, navigateTo: (Int) -> Unit) {

    @Composable
    fun Cell(item: Any, modifier: Modifier) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.toString(),
                modifier = modifier.drawWithContent {
                    if (readyToDraw) drawContent()
                },
                style = textStyle,
                maxLines = 1,
                softWrap = false,
                onTextLayout = { textLayoutResult ->
                    if (textLayoutResult.didOverflowWidth) {
                        setTextStyle(textStyle.copy(fontSize = textStyle.fontSize * 0.9))
                    } else {
                        setReadyToDraw(true)
                    }
                }
            )
        }
    }


    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val cells: List<Pair<Any, Float>> = listOf(
            toCompressedTimeStamp(trackedMetrics.duration.toMillis()) to 1.5f,
            String.format("%.2f", trackedMetrics.percentage) to 1.2f,
            trackedMetrics.sessions to 0.8f
        )

        Surface(
            Modifier
                .fillMaxWidth()
                .weight(2f)
                .padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
                .clickable { navigateTo(trackedMetrics.timeTracker.uid) },
            shape = RoundedCornerShape(25),
        ) {
            Cell(
                trackedMetrics.timeTracker.title,
                Modifier
            )
        }

        cells.mapIndexed { index, item ->
            Cell(
                item.first,
                Modifier
                    .fillMaxWidth()
                    .weight(item.second)
                    .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
fun Table(weeklyData: TimeTrackerChartData, sortBy: (TableSortType) -> Unit, navigateTo: (Int) -> Unit) {

    val textStyleBody1 = MaterialTheme.typography.body1
    var textStyle by remember { mutableStateOf(textStyleBody1) }
    var readyToDraw by remember { mutableStateOf(false) }

    var sortType: TableSortType by remember { mutableStateOf(NoSort()) }

    fun updateSort(newSortType: TableSortType) {
        if (sortType::class == newSortType::class) sortType.descending = !sortType.descending
        else sortType = newSortType
        sortBy(sortType)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
        ) {
//            val titles = mapOf(
//                //"Tracker" to { updateSort(Alphabetical()) },
//                "Duration" to { updateSort(com.timesheet.app.ui.table.Duration()) } to 1f,
//                "Relative" to { updateSort(com.timesheet.app.ui.table.Duration()) } to 1f,
//                "Sessions" to { updateSort(Sessions()) } to 1f
//            )
            val titles = listOf(
                ChartTitleData("Duration", 1.5f) { updateSort(com.timesheet.app.ui.table.Duration()) },
                ChartTitleData("Relative", 1.2f) { updateSort(com.timesheet.app.ui.table.Duration()) },
                ChartTitleData("Sessions", 0.8f) { updateSort(Sessions()) }
            )


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),

            ) {
                ChartTitle("Tracker") { updateSort(Alphabetical()) }
            }
            titles.map {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(it.weight),
                ) {
                    ChartTitle(it.title) { it.onClick() }
                }
            }
        }
        weeklyData.tracked.mapIndexed { rowIndex, trackedMetrics ->
            val color = if(rowIndex % 2 == 0) White else Color.White
            TableRow(
                modifier = Modifier.background(color),
                trackedMetrics = trackedMetrics,
                textStyle = textStyle,
                readyToDraw = readyToDraw,
                setTextStyle = { textStyle = it },
                setReadyToDraw = { readyToDraw = it }
            ) { navigateTo(it) }
        }
    }
}

data class ChartTitleData(
    val title: String,
    val weight: Float,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalTextApi::class)
@Composable
fun PieChart(originalData: List<PieChartSlice>) {

    val data = originalData
    val dataSum: Long = data.map { it.data }.sum()

    var chartSize by remember { mutableStateOf(0f) }
    var selected: PieChartSlice? by remember { mutableStateOf(null) }

//    Log.v("chartSize", chartSize.toString())
//
    data.forEach { Log.v("DATAaaa", it.toString()) }

    val labelStyle = MaterialTheme.typography.body1
    fun Long.asAngle(): Float = this/dataSum.toFloat() * 360f

    fun updateSelectedPieSlice(angle: Float) {

        Log.v("START ANGLE", angle.toString())
        data.forEach { Log.v("DATA", it.toString()) }
        Log.v("DATA VALUES", data.size.toString())

        var currentSum = 0L
        data.asReversed().forEach {
            Log.v("IN FOREACH SEL", it.toString())
            currentSum += it.data
            if(currentSum.asAngle().also {Log.v("ANGLE", it.toString()) } >= angle) {
                selected = if((selected?.uid ?: -1) == it.uid) null
                else it
                Log.v("SELECTED", "SELECTED is $selected")
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
            val radius = width/2
            val strokeWidth = 20.dp.toPx()

            var startAngle = 0f

            for(index in 0..data.lastIndex) {
                val chartData = data[index]
                val sweepAngle = chartData.data.asAngle()

                val adjustedStrokeWidth = if(chartData.uid == (selected?.uid ?: -1)) strokeWidth / 2 else strokeWidth

                drawArc(
                    color = chartData.color,
                    startAngle = startAngle + 9,
                    sweepAngle = sweepAngle - 9,
                    useCenter = false,
                    topLeft = Offset(strokeWidth/2, strokeWidth/2),
                    size = Size(width-strokeWidth, width-strokeWidth),
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
data class PieChartSlice(val color: Color, val data: Long, val title: String, val uid: Int, val content: @Composable () -> Unit)

@Composable
fun Legend(items: List<Pair<Color, String>>) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        items.map {
            Row(
                verticalAlignment = CenterVertically,
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

data class Section(
    val title: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)
@Composable
fun Sections(sections: Map<Any, Section>) {
    var section by remember {
        mutableStateOf(
            if(sections.isEmpty()) null else sections.keys.first()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            sections.map {
                IconButton(
                    onClick = { section = it.key },
                    modifier = Modifier.weight(1f)
                        .background(
                            if(section?.equals(it.key) == true) Color.LightGray.copy(alpha = 0.3f) else Color.Transparent
                        )
                ) {
                    Icon(
                        it.value.icon,
                        "Button"
                    )
                }
            }
        }
        section?.let {
            val currentSection = sections[section]
            currentSection?.let { it.content() }
        }
    }
}

@Composable
fun Home() {
    Text("Home")
}

@Composable
fun List() {
    Text("List")
}