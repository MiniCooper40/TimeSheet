package com.timesheet.app.ui

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
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
import com.timesheet.app.data.entity.GroupWithTrackers
import com.timesheet.app.data.entity.TimeTracker
import com.timesheet.app.data.entity.TrackerGroup
import com.timesheet.app.theme.wearColorPalette
import com.timesheet.app.ui.pie.Legend
import com.timesheet.app.ui.pie.PieChart
import com.timesheet.app.ui.pie.PieChartSlice
import com.timesheet.app.ui.table.Table
import com.timesheet.app.view.model.HomePageViewModel
import com.timesheet.app.view.model.TimeSheetViewModel
import java.util.Random


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupPreview(
    groupWithTrackers: GroupWithTrackers,
    selected: Boolean = false,
    onClick: () -> Unit,
    onSelect: () -> Unit
) {
    val group = groupWithTrackers.group
    val trackers = groupWithTrackers.trackers

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onSelect
            )
            .border(
                if (selected) BorderStroke(2.dp, wearColorPalette.onBackground)
                else BorderStroke(0.dp, Color.Transparent),
                RoundedCornerShape(4.dp)
            ),
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
            Log.v("GROUP", "TITLE is ${group.title}, TRACKERS are ${trackers.map { it.title }}")
            Text(group.title, style = MaterialTheme.typography.h4)
            MiniTrackerChipFlowRow(
                trackers = trackers,
                elevation = 0.dp,
                maxTrackers = 2,
                modifier = Modifier.background(Color.Transparent)
            )
//            else Icon(
//                Icons.Default.Check,
//                "Check mark"
//            )
        }
//        if(selected) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Blue.copy(alpha = 0.1f)),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    Icons.Default.Check,
//                    "Check mark icon"
//                )
//            }
//        }
    }
}

@Composable
fun HomePage(
    groups: List<GroupWithTrackers>,
    deleteGroups: (List<Int>) -> Unit,
    navigateTo: (Int) -> Unit
) {

    var selected: Set<GroupWithTrackers> by remember { mutableStateOf(mutableSetOf()) }
    var confirmationAlertActive by remember { mutableStateOf(false) }

    if (confirmationAlertActive) {
        TimeSheetAlert(
            onDismissRequest = { confirmationAlertActive = false },
            onConfirm = {
                deleteGroups(selected.map { it.group.uid } )
                confirmationAlertActive = false
                selected = setOf()
            },
            title = "Delete Trackers"
        ) {
            Column {
                Text("Are you sure you want to delete the following groups?")
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    selected.map { Text(it.group.title) }
                }
            }
        }
    }

    Box {
        VerticalScrollArea {
            SectionColumn {
                Section(
                    title = "Groups"
                ) {
                    groups.map {

                        val onSelect = {
                            Log.v("TOGGLE SELECTED", it.toString())
                            if (selected.contains(it)) selected -= it
                            else selected += it
                        }


                        val onClick = {
                            navigateTo(it.group.uid)
                        }

                        GroupPreview(
                            groupWithTrackers = it,
                            selected = selected.contains(it),
                            onClick = if (selected.isEmpty()) onClick else onSelect,
                            onSelect = onSelect
                        )

                    }
                }
            }
        }
    }

    if (selected.isNotEmpty()) {
        SelectionBar(
            actions = listOf(
                Icons.Default.Clear to { selected = setOf() },
                Icons.Default.Delete to { confirmationAlertActive = true }
            )
        )
    }
}