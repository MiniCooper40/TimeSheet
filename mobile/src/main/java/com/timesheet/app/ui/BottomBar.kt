package com.timesheet.app.ui

import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable

@Composable
fun BottomBar(onHomeClick: () -> Unit, onListClick: () -> Unit, state: String) {

    BottomNavigation {
        BottomNavigationItem(selected = state.equals("home"), onClick = { onHomeClick() }, icon = { Icon(Icons.Default.Home, "Button for tracker home") })
        BottomNavigationItem(selected = state.equals("list"), onClick = { onListClick() }, icon = { Icon(Icons.Default.List, "Button for list of trackers") })
    }
}