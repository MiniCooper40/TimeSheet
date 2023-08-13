package com.timesheet.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timesheet.app.data.model.TimeTracker
import com.timesheet.app.data.model.TrackedTimes
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun TimeTrackerStamp(tracker: TimeTracker, default:String ="", modifier: Modifier = Modifier, style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.h5) {

    var currentTime by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    LaunchedEffect(key1 = currentTime, block = {
        delay(100L)
        currentTime = System.currentTimeMillis()
    })

    val text =
        if(tracker.startTime != 0L) toTimeStamp(currentTime - tracker.startTime!!)
        else default

    Text(text, style = style, modifier = modifier)
}

@Composable
fun TrackerChip(state: TrackedTimes?, onClick: () -> Unit, toggleTracking: () -> Unit) {



    fun toggle() {
        toggleTracking()
    }

    state?.let {
        val tracker = it.timeTracker
        val times = it.trackedTimes

        val timesDeltas = times.map { it.endTime - it.startTime }.filter { it < 300000 }


        Card(
            modifier = Modifier
                .clickable { onClick()  }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = tracker.title, style = MaterialTheme.typography.h4)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TimeTrackerStamp(tracker = tracker)
                        IconButton(
                            onClick = { toggleTracking() }
                        ) {
                            val icon = if(tracker.startTime == 0L) Icons.Default.PlayArrow else Icons.Default.Done
                            Icon(icon,
                                "View more for this tracker",
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                }
                Row {
                    val lastTracked = if(times.size > 0) Date(times.last().endTime).toString() else "never"
                    Text("last tracked: $lastTracked")
                }
            }
        }
    }

}