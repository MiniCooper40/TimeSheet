package com.timesheet.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timesheet.app.data.entity.GroupWithTrackers
import com.timesheet.app.theme.wearColorPalette
import com.timesheet.app.ui.pie.Legend
import com.timesheet.app.ui.pie.PieChart
import com.timesheet.app.ui.pie.PieChartSlice
import com.timesheet.app.ui.table.Table
import com.timesheet.app.view.model.HomePageViewModel
import com.timesheet.app.view.model.TimeSheetViewModel
import java.util.Random


@Composable
fun GroupPreview(groupWithTrackers: GroupWithTrackers, onClick: () -> Unit) {
    val group = groupWithTrackers.group
    val trackers = groupWithTrackers.trackers

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        backgroundColor = wearColorPalette.background,
        elevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabeledEntry(group.title) {
                MiniTrackerChipFlowRow(trackers)
            }
        }
    }
}

@Composable
fun HomePage(timeSheetViewModel: TimeSheetViewModel, navigateTo: (Int) -> Unit) {

    val groups by timeSheetViewModel.trackerGroups.collectAsState()

    SectionColumn {
        Section(title = "Groups") {
            groups.map {
                GroupPreview(groupWithTrackers = it) {
                    navigateTo(it.group.uid)
                }
            }
        }
    }


}