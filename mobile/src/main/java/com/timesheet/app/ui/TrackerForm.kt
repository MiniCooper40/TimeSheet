package com.timesheet.app.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.timesheet.app.data.model.TimeTracker
import com.timesheet.app.view.TimeSheetViewModel

@Composable
fun TrackerForm(timeSheetViewModel: TimeSheetViewModel, context: Context = LocalContext.current) {
    var title by remember{ mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(value = title, onValueChange = {title = it})
        Button(
            onClick = {
                val tracker = TimeTracker(title=title)
                timeSheetViewModel.createTracker(context, tracker)
                title = ""
            }
        ) {
            Text("Create")
        }
    }
}