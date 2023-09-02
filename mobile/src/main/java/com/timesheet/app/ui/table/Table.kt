package com.timesheet.app.ui.table

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.timesheet.app.presentation.theme.White
import com.timesheet.app.ui.toCompressedTimeStamp
import com.timesheet.app.view.model.TimeTrackerChartData
import com.timesheet.app.view.model.TrackerMetrics

@Composable
fun ChartTitle(title: String, onClick: () -> Unit) {
    Text(
        style = MaterialTheme.typography.caption,
        modifier = Modifier
            .clickable { onClick() },
        textAlign = TextAlign.Center,
        text = title
    )
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
fun TableRow(
    trackedMetrics: TrackerMetrics,
    modifier: Modifier = Modifier,
    textStyle: TextStyle,
    readyToDraw: Boolean,
    setTextStyle: (TextStyle) -> Unit,
    setReadyToDraw: (Boolean) -> Unit,
    navigateTo: (Int) -> Unit
) {

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
fun Table(
    chartData: TimeTrackerChartData,
    sortBy: (TableSortType) -> Unit,
    navigateTo: (Int) -> Unit
) {

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
            val titles = listOf(
                ChartTitleData(
                    "Duration",
                    1.5f
                ) { updateSort(Duration()) },
                ChartTitleData(
                    "Relative",
                    1.2f
                ) { updateSort(Duration()) },
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
        chartData.tracked.mapIndexed { rowIndex, trackedMetrics ->
            val color = if (rowIndex % 2 == 0) White else Color.White
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