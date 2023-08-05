package com.timesheet.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            WearApp("Android")
        }
    }
}

@Composable
fun WearApp(greetingName: String) {

    MaterialTheme {
        Text(greetingName)
    }
}
