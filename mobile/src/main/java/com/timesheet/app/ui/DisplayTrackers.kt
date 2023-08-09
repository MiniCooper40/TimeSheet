package com.timesheet.app.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.timesheet.app.view.TimeSheetViewModel

@Composable
fun DisplayTrackers(
    maxNumberOfTrackers: Int = Int.MAX_VALUE,
    timeSheetViewModel: TimeSheetViewModel,
    navController: NavController
) {
    val state by timeSheetViewModel.timeTrackers.collectAsState()

    Log.v("NUM TRACKERS", maxNumberOfTrackers.toString())

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        state.trackers.take(maxNumberOfTrackers).map {
            Button(
                modifier = Modifier
                    .width(220.dp),
                onClick = { navController.navigate("tracker/" + it.uid) }
            ) {
                Text(it.title)
            }
        }
    }
}