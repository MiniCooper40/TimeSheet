package com.timesheet.app.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timesheet.app.data.entity.TimeTracker
import com.timesheet.app.theme.wearColorPalette
import com.timesheet.app.ui.pie.Legend
import com.timesheet.app.ui.pie.PieChart
import com.timesheet.app.ui.pie.PieChartSlice
import com.timesheet.app.ui.table.MiniTrackerChip
import com.timesheet.app.ui.table.NoSort
import com.timesheet.app.ui.table.Table
import com.timesheet.app.view.model.TrackerGroupViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MiniTrackerChipFlowRow(
    trackers: List<TimeTracker>,
    navigateToTracker: ((Int) -> Unit)? = null
) {
    FlowRow {
        trackers.map { tracker ->
            Surface(
                elevation = 3.dp,
                color = wearColorPalette.background,
                modifier = Modifier.padding(5.dp)
            ) {
                MiniTrackerChip(
                    modifier = Modifier
                        .padding(all = 4.dp)
                        .clickable {
                            navigateToTracker?.let {
                                it(tracker.uid)
                            }
                        },
                    tracker = tracker
                )
            }
        }

    }
}

@Composable
fun SectionColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        content()
    }
}

@Composable
fun GroupDetails(uid: Int, navigateToTracker: (Int) -> Unit) {
    val trackerGroupViewModel: TrackerGroupViewModel =
        viewModel(factory = TrackerGroupViewModel.factoryFor(uid))

    val groupWithTrackers by trackerGroupViewModel.trackerGroup.collectAsState()
    val trackerChartData by trackerGroupViewModel.trackerChartData.collectAsState()

    val trackers = groupWithTrackers.trackers
    val group = groupWithTrackers.group

    Log.v("CHART DATA", trackerChartData.toString())

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        Banner(group.title) {
            Text("${trackers.size} trackers.")
        }
        SectionColumn {
            Section(title = "Trackers in group") {
                MiniTrackerChipFlowRow(trackers) { navigateToTracker(it) }
            }
            Section(title = "Table") {
                Table(
                    chartData = trackerChartData,
                    sortBy = { trackerGroupViewModel.sortTrackerChartDataBy(it) },
                    navigateTo = navigateToTracker
                )
            }
            Section(
                title = "Pie chart",
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PieChart(
                    trackerChartData.tracked.map { metrics ->
                        val tracker = metrics.timeTracker
                        val value = metrics.duration.toMillis()
                        PieChartSlice(
                            color = Color(tracker.color),
                            data = value,
                            title = tracker.title,
                            uid = tracker.uid
                        ){
                            Text(tracker.title)
                            Text(
                                toCompressedTimeStamp(metrics.duration.toMillis()),
                                style = MaterialTheme.typography.h3
                            )
                            Text(
                                "${
                                    String.format(
                                        "%.2f",
                                        metrics.percentage
                                    )
                                }%. ${metrics.sessions} session${if (metrics.sessions == 1) "" else "s"}."
                            )
                        }
                    },
                    fillMaxWidth = 0.6f
                )
                Legend(
                    trackers.map { Color(it.color) to it.title }
                )
            }
        }
    }
}