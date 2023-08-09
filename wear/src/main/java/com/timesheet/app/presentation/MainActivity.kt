/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.timesheet.app.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.app.timesheet.R
import com.google.android.gms.wearable.Wearable
import com.timesheet.app.presentation.data.model.TimeTracker
import com.timesheet.app.presentation.theme.TimeSheetTheme
import com.timesheet.app.presentation.view.TimeSheetViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    val model: TimeSheetViewModel by viewModels {
        TimeSheetViewModel.Factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            WearApp( model, application.applicationContext)
        }
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(model)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(model)
    }
}

@Composable
fun WearApp(model: TimeSheetViewModel, applicationContext: Context) {

    val state by model.timeSheetState.collectAsState()

    TimeSheetTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .verticalScroll(rememberScrollState())
                .padding(all = 20.dp)
                .padding(top = 35.dp)
                ,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            Image(
//                painter = painterResource(id = R.drawable.time_sheet_logo_black),
//                contentDescription = "Logo, white",
//                modifier = Modifier
//                    .width(82.dp)
//            )
            state.trackers.map { TimeTrackerChip(it, { model.updateTrackerStartTime(applicationContext, it) }) }
        }
    }
}

fun fillTimeStampZeros(time: Int) : String {
    return if(time < 10) "0${time}" else time.toString()
}

fun toTimeStamp(milliseconds: Long?) : String {

    if(milliseconds == null) return "0"

    val seconds = (milliseconds / 1000).toInt() % 60
    val minutes = (milliseconds / (1000 * 60) % 60).toInt()
    val hours = (milliseconds / (1000 * 60 * 60) % 24).toInt()

    return "${fillTimeStampZeros(hours)}:${fillTimeStampZeros(minutes)}:${fillTimeStampZeros(seconds)}"
}

@Composable
fun TimeTrackerChip(
    timeTracker: TimeTracker,
    onToggleTracking: () -> Unit
) {


    var currentTime by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    LaunchedEffect(key1 = currentTime, block = {
        delay(100L)
        currentTime = System.currentTimeMillis()
    })

    Chip(
        onClick = onToggleTracking,
        colors = ChipDefaults.chipColors(),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 10.dp)
        ) {

            val text =
                if(timeTracker.startTime != 0L) " " + toTimeStamp(currentTime - timeTracker.startTime!!)
                else ""

            Text(
                text = timeTracker.title + text,
                textAlign = TextAlign.Center
            )
        }
    }
}



@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
}