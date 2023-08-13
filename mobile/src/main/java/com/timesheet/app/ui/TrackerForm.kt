package com.timesheet.app.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.app.timesheet.R
import com.timesheet.app.data.model.TimeTracker
import com.timesheet.app.view.TimeSheetViewModel

@Composable
fun TrackerForm(timeSheetViewModel: TimeSheetViewModel, context: Context = LocalContext.current) {
    var title by remember{ mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(top = 70.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.time_sheet_logo_black),
            contentDescription = "logo, black",
            modifier = Modifier
                .size(200.dp)
        )
        TextField(
            value = title,
            onValueChange = {title = it},
            modifier = Modifier
                .border(BorderStroke(0.dp, Color.White)),
            label = {
                Text("Title")
            }
        )
        Button(
            onClick = {
                val tracker = TimeTracker(title =title)
                timeSheetViewModel.createTracker(context, tracker)
                title = ""
            },
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text("Create")
        }
    }
}