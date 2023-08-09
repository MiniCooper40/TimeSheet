package com.timesheet.app.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.materialIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.timesheet.app.view.TimeSheetViewModel


@Composable
fun HomePage(navController: NavController, timeSheetViewModel: TimeSheetViewModel) {

    var page by remember{ mutableStateOf("home") }

    val onHomeClick = {
        if(!page.equals("home")) page = "list"
    }

    val onListClick = {
        if(!page.equals("list")) page = "home"
    }

    Log.v("state", page)

    Scaffold(
           content = {
               Column(
                   modifier = Modifier
                       .padding(it)
                       .fillMaxWidth(),
                   horizontalAlignment = Alignment.CenterHorizontally
               ) {
                   TrackerForm(timeSheetViewModel = timeSheetViewModel)
               }
           },
        bottomBar = { BottomBar(onHomeClick, onListClick, page) },
        floatingActionButton = {
            FloatingActionButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.Add, "Create new tracker")
            }
        }
    )
}

@Composable
fun Home() {
    Text("Home")
}

@Composable
fun List() {
    Text("List")
}