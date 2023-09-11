package com.timesheet.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.timesheet.app.data.entity.TimeTracker
import com.timesheet.app.data.entity.TrackedTimes
import com.timesheet.app.theme.wearColorPalette
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackerChip(
    state: TrackedTimes?,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    onSelected: () -> Unit,
    toggleTracking: () -> Unit
) {

    state?.let {
        val tracker = it.timeTracker
        val times = it.trackedTimes

        Card(
            modifier = Modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onSelected
                )
                .border(
                    if(selected) BorderStroke(2.dp, wearColorPalette.onBackground)
                    else BorderStroke(0.dp, Color.Transparent),
                    RoundedCornerShape(4.dp)
                ),
            backgroundColor = wearColorPalette.background,
            elevation = 3.dp

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = tracker.title, style = MaterialTheme.typography.h4)
                        Circle(size = 20.dp, color = tracker.color)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TimeTrackerStamp(tracker = tracker)
                        IconButton(
                            onClick = { toggleTracking() },
                            enabled = enabled
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
                    val lastTracked = if(times.isNotEmpty()) Date(times.last().endTime).toString() else "never"
                    Text("last tracked: $lastTracked")
                }
            }
        }
    }

}