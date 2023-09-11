package com.timesheet.app.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.timesheet.app.data.entity.TimeTracker
import com.timesheet.app.theme.Grey
import com.timesheet.app.theme.wearColorPalette
import com.timesheet.app.view.model.TimeSheetViewModel
import kotlinx.coroutines.delay


@Composable
fun DisplayTrackers(
    timeSheetViewModel: TimeSheetViewModel,
    trackers: List<TimeTracker>,
    maxNumberOfTrackers: Int = Int.MAX_VALUE,
    navigateTo: (Int) -> Unit,
    deleteTrackers: (List<Int>) -> Unit,
    createTrackerWithIds: (IntArray) -> Unit = {},
    context: Context = LocalContext.current
) {

    //val state by timeSheetViewModel.timeTrackers.collectAsState()
    var selected by remember { mutableStateOf(setOf<TimeTracker>()) }

    var confirmationAlertActive by remember { mutableStateOf(false) }

    Log.v("TRACKERS", trackers.map { it.title }.toString())

    if (confirmationAlertActive) {
        TimeSheetAlert(
            onDismissRequest = { confirmationAlertActive = false },
            onConfirm = {
                deleteTrackers(selected.map { it.uid })
//                timeSheetViewModel.deleteTrackersByUid(selected.map { it.uid })
                confirmationAlertActive = false
                selected = setOf()
            },
            title = "Delete Trackers"
        ) {
            Column {
                Text("Are you sure you want to delete the following trackers?")
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    selected.map { Text(it.title) }
                }
            }
        }
    }

    Box {
        VerticalScrollArea {
            SectionColumn {
                Section(title = "Trackers") {
                    trackers.take(maxNumberOfTrackers).map {
                        val trackedTimes by timeSheetViewModel.trackedTimesFor(it.uid)
                            .collectAsState(initial = null)
                        trackedTimes?.let {
                            val timeTracker = it.trackedTimes.timeTracker

                            val onSelected = {
                                if (selected.contains(timeTracker)) selected -= timeTracker
                                else selected += timeTracker
                            }

                            val onClick = { navigateTo(timeTracker.uid) }
                            TrackerChip(
                                it.trackedTimes,
                                enabled = selected.isEmpty(),
                                selected = selected.contains(timeTracker),
                                onClick = if (selected.isEmpty()) onClick else onSelected,
                                onSelected = onSelected,
                                toggleTracking = {
                                    timeSheetViewModel.updateTrackerStartTime(
                                        context,
                                        it.trackedTimes.timeTracker
                                    )
                                }
                            )
                        }
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
            ),
            textButton = "Create group with selected" to {
                createTrackerWithIds(selected.map { it.uid }.toIntArray())
            }
        )
    }
//    Box(
//        contentAlignment = Alignment.BottomCenter,4
//        modifier = Modifier.fillMaxSize()
//    ) {
//        if(selected.isNotEmpty()) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(wearColorPalette.primary),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                TextButton(onClick = { createTrackerWithIds(selected.map { it.uid }.toIntArray()) }) {
//                    Text("Create group with selected", color = wearColorPalette.onPrimary)
//                }
//                Row {
//                    IconButton(onClick = { selected = setOf() }) {
//                        Icon(
//                            Icons.Default.Clear,
//                            "Clear",
//                            tint = wearColorPalette.onPrimary
//                        )
//                    }
//                    IconButton(onClick = { confirmationAlertActive = true }) {
//                        Icon(
//                            Icons.Default.Delete,
//                            "Delete",
//                            tint = wearColorPalette.onPrimary
//                        )
//                    }
//                }
//            }
//        }
//    }
}