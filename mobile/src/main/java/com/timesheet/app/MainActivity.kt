package com.timesheet.app.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
//            WearApp("Android")
            Button(
                onClick={ testCommunication(this) }
            ) {
                Text("Button!!")
            }
        }
    }
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
