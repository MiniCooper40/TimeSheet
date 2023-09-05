package com.timesheet.app.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.app.timesheet.R
import com.timesheet.app.data.entity.TimeTracker
import com.timesheet.app.theme.wearColorPalette
import com.timesheet.app.view.model.TimeSheetViewModel
import java.util.Arrays


private enum class PopupType {
    Group, Tracker
}





@Composable
fun TrackerForm(
    timeSheetViewModel: TimeSheetViewModel,
    navigateToTrackerForm: () -> Unit,
    navigateToGroupForm: () -> Unit
) {

    val groups by timeSheetViewModel.trackerGroups.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.time_sheet_logo_black),
                contentDescription = "logo, black",
                modifier = Modifier
                    .size(200.dp)
            )
            TextButton(
                onClick = navigateToTrackerForm
            ) {
                Text("Create tracker")
            }
            TextButton(
                onClick = navigateToGroupForm
            ) {
                Text("Create group")
            }

            Log.v("Groups", Arrays.deepToString(groups.toTypedArray()))

            groups.forEach {groupWithTrackers ->

                //Log.v("GROUP", groupWithTrackers.toString())

                val group = groupWithTrackers.group
                val trackers = groupWithTrackers.trackers

                Row {

                    Text(group.title)
                    Column {
                        trackers.map {tracker ->
                            Text(tracker.title)
                        }
                    }
                }
            }

        }

//        when (openPopup) {
//            PopupType.Tracker -> {
//                FormPopup(onDismissRequest = { openPopup = null }, onSubmit = {
//                    if (trackerTitle.text.isNotEmpty()) {
//                        timeSheetViewModel.createTracker(
//                            context = context,
//                            tracker = TimeTracker(
//                                trackerTitle.text
//                            )
//                        )
//                        openPopup = null
//                    }
//                }) {
//                    FormTextInput(title = "Tracker name", value = trackerTitle) {
//                        trackerTitle = it
//                    }
//                }
//            }
//
//            PopupType.Group -> {
//                FormPopup(
//                    onDismissRequest = { openPopup = null },
//                    onSubmit = { Log.v("Submit", "GROUP FORM SUBMITTED.") }) {
////                    FormTextInput(title = "Group name", value = groupTitle) {
////                        groupTitle = it
////                    }
//                }
//            }
//
//            null -> {
//
//            }
//        }
    }
}