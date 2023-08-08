package com.timesheet.app.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.timesheet.app.data.model.TimeTracker
import com.timesheet.app.data.model.TrackedTime
import com.timesheet.app.data.model.TrackedTimes
import com.timesheet.app.view.TimeSheetViewModel
import com.timesheet.app.view.TimeTrackerViewModel
import java.util.Date


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val timeSheetViewModel: TimeSheetViewModel by viewModels { TimeSheetViewModel.Factory }



        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "start") {
                composable("start") { TrackerForm(applicationContext, timeSheetViewModel, navController) }
                composable("tracker/{uid}", arguments = listOf(navArgument("uid") { type = NavType.IntType })) {
                    it.arguments?.getInt("uid")?.let { uid ->
                        TrackerDetails(
                            context = applicationContext,
                            timeSheetViewModel,
                            uid = uid
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainApp(context: Context, timeSheetViewModel: TimeSheetViewModel) {


}

fun navigateToTracker(uid: Int) {

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
fun TrackerDetails(context: Context, timeSheetViewModel: TimeSheetViewModel, uid: Int) {


    val state by timeSheetViewModel.trackedTimesFor(uid).collectAsState(null)

    state?.let {
        Column {
            Text(it.timeTracker.title)
            it.trackedTimes.map { TrackedTime(it) }
        }
    }
}

@Composable
fun TrackedTime(trackedTime: TrackedTime) {
    val elapsedTime = trackedTime.let { toTimeStamp(it.endTime - it.startTime) }
    val startDate = Date(trackedTime.startTime)

    Row {
        Text(elapsedTime)
        Text(startDate.toString())
    }
}

@Composable
fun TrackerForm(
    context: Context,
    timeSheetViewModel: TimeSheetViewModel,
    navController: NavHostController
) {
    
    var title by remember{ mutableStateOf("") }
    
    Column {
        TextField(value = title, onValueChange = {title = it})
        Button(
            onClick = {
                val tracker = TimeTracker(title=title)
                timeSheetViewModel.createTracker(context, tracker)
//                testData(context, tracker)
                title = ""
            }
        ) {
            Text("Create")
        }
        TrackerDisplay(timeSheetViewModel = timeSheetViewModel, navController)
    }
}

@Composable
fun TrackerDisplay(timeSheetViewModel: TimeSheetViewModel, navController: NavHostController) {
    val state by timeSheetViewModel.timeTrackers.collectAsState()
    
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        state.trackers.map { 
            Button(
                onClick = { navController.navigate("tracker/" + it.uid) }
            ) {
                Text(it.title)
            }
        }
    }
}

fun testData(context: Context, tracker: TimeTracker) {


    Thread {
        val putDataMapRequest = PutDataMapRequest.create("/tracker/create")

        val dataMap = putDataMapRequest.dataMap

        dataMap.putLong("created", System.currentTimeMillis())
        dataMap.putString("title", tracker.title)
        dataMap.putLong("startTime", tracker.startTime)
        dataMap.putInt("uid", tracker.uid)

        val dataRequest = putDataMapRequest.asPutDataRequest()

        Wearable.getDataClient(context).putDataItem(putDataMapRequest.asPutDataRequest())
    }.start()
}

fun testCommunication(context: Context) {
    Log.v("MOBILE", "Clicked test")

    Thread {
        val nodeListTask: Task<List<Node>> =
            Wearable.getNodeClient(context).connectedNodes

        val nodes = Tasks.await(nodeListTask)
        for(node in nodes) {
            // Build the message
            // Build the message
            val message = "Hello!"
            val payload = message.toByteArray()

            // Send the message

            // Send the message
            val sendMessageTask = Wearable.getMessageClient(context)
                .sendMessage(node.id, "/message", payload)

            sendMessageTask.addOnSuccessListener {
                Log.v("TASK", "Success")
            }
            sendMessageTask.addOnCanceledListener {
                Log.v("TASK", "Cancelled")
            }
            sendMessageTask.addOnFailureListener {
                Log.v("TASK", "Failure")
            }

        }
    }.start()
}

@Composable
fun WearApp(greetingName: String) {

    MaterialTheme {
        Text(greetingName)
    }
}
