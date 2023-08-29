package com.timesheet.app.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
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
import androidx.navigation.AnimBuilder
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import com.app.timesheet.R
import com.timesheet.app.view.TimeSheetViewModel
import kotlinx.coroutines.delay


@Composable
fun DisplayTrackers(
    maxNumberOfTrackers: Int = Int.MAX_VALUE,
    timeSheetViewModel: TimeSheetViewModel,
    navigateTo: (Int) -> Unit,
    context: Context = LocalContext.current
) {
    Display {

        val state by timeSheetViewModel.timeTrackers.collectAsState()
        var currentTime by remember {
            mutableStateOf(System.currentTimeMillis())
        }

        LaunchedEffect(key1 = currentTime, block = {
            delay(100L)
            currentTime = System.currentTimeMillis()
        })


        Log.v("NUM TRACKERS", maxNumberOfTrackers.toString())

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            state.trackers.take(maxNumberOfTrackers).map {
                val state by timeSheetViewModel.trackedTimesFor(it.uid).collectAsState(initial = null)
                state?.let {
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