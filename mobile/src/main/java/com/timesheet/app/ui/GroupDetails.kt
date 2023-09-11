package com.timesheet.app.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
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
    modifier: Modifier =  Modifier,
    trackers: List<TimeTracker>,
    elevation: Dp = 3.dp,
    maxTrackers: Int = Int.MAX_VALUE,
    navigateToTracker: ((Int) -> Unit)? = null
) {

    val adjustedMaxTrackers = if (maxTrackers > trackers.size) trackers.size else maxTrackers

    val chipModifier = modifier
        .padding(all = 4.dp)

    FlowRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        trackers.take(adjustedMaxTrackers).map { tracker ->
            Surface(
                elevation = elevation,
                color = wearColorPalette.background,
                modifier = Modifier.padding(5.dp)
            ) {
                MiniTrackerChip(
                    modifier = chipModifier
                        .clickable {
                            navigateToTracker?.let {
                                it(tracker.uid)
                            }
                        },
                    tracker = tracker
                )
            }
        }
        if (adjustedMaxTrackers < trackers.size) {
            val numTrackersLeft = trackers.size - adjustedMaxTrackers
            Surface(
                elevation = elevation,
                color = wearColorPalette.background,
                modifier = Modifier.padding(5.dp)
            ) {
                MiniTrackerChip(
                    content = {
                        Text("$numTrackersLeft other${if(numTrackersLeft == 1) "" else "s"}...")
                    }
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
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        content()
        Spacer(modifier = Modifier.fillMaxWidth().height(30.dp))
    }
}

@Composable
fun GroupDetails(uid: Int, navigateToTracker: (Int) -> Unit, editGroup: () -> Unit) {
    val trackerGroupViewModel: TrackerGroupViewModel =
        viewModel(factory = TrackerGroupViewModel.factoryFor(uid))

    val groupWithTrackers by trackerGroupViewModel.trackerGroup.collectAsState()
    val trackerChartData by trackerGroupViewModel.trackerChartData.collectAsState()

    val trackers = groupWithTrackers.trackers
    val group = groupWithTrackers.group

    Log.v("CHART DATA", trackerChartData.tracked.map { it.timeTracker.title }.toString())
    Log.v("TRACKERS", trackers.map { it.title }.toString())

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        Banner(group.title) {
            Text("${trackers.size} trackers.")
            IconButton(onClick = editGroup) {
                Icon(
                    Icons.Default.Edit,
                    "Edit"
                )
            }
        }
        SectionColumn {
            Section(title = "Trackers in group") {
                MiniTrackerChipFlowRow(trackers = trackers) { navigateToTracker(it) }
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
                        ) {
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