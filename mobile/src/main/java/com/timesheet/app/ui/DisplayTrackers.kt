package com.timesheet.app.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.timesheet.app.view.model.TimeSheetViewModel
import kotlinx.coroutines.delay


@Composable
fun DisplayTrackers(
    maxNumberOfTrackers: Int = Int.MAX_VALUE,
    timeSheetViewModel: TimeSheetViewModel,
    navigateTo: (Int) -> Unit,
    context: Context = LocalContext.current
) {

    val state by timeSheetViewModel.timeTrackers.collectAsState()
    var currentTime by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    LaunchedEffect(key1 = currentTime, block = {
        delay(100L)
        currentTime = System.currentTimeMillis()
    })

    VerticalScrollArea {
        SectionColumn {
            Section(title = "Trackers") {
                state.trackers.take(maxNumberOfTrackers).map {
                    val trackedTimes by timeSheetViewModel.trackedTimesFor(it.uid).collectAsState(initial = null)
                    trackedTimes?.let {
                        TrackerChip(
                            it.trackedTimes,
                            onClick = { navigateTo(it.trackedTimes.timeTracker.uid) },
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