package com.timesheet.app.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
inline fun RecentTrackers(Trackers: () -> Unit) {
    Text("Your recent trackers")
    Trackers()
}

@Composable
inline fun ActiveTrackers(trackers: () -> Unit) {
    Text("Your active trackers")

    trackers()
}