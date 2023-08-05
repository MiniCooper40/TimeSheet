package com.timesheet.app.presentation.communication

import android.util.Log
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class TimeSheetListenerService: WearableListenerService() {

    init {
        Log.v("WEARABLE LISTENER", "In INIT")
    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        Log.v("WEARABLE LISTENER", "In wearable listener, onDataChanged")
        dataEventBuffer.forEach {
            when(it.type) {
                DataEvent.TYPE_CHANGED -> onTypeChangeEvent(it)
                else -> onDeleteEvent(it)
            }
        }
    }

    private fun onTypeChangeEvent(dataEvent: DataEvent) {
//        val dataItem: DataItem = dataEvent.dataItem
//
//        DataMapItem.fromDataItem(dataItem).dataMap.apply {
//            Log.v("print", this.toString())
//        }
        Log.v("WEARABLE LISTENER", "In wearable listener, onTypeChangeEvent")
    }

    private fun onDeleteEvent(dataEvent: DataEvent) {
        Log.v("WEARABLE LISTENER", "In wearable listener, onDeleteEvent")
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        Log.v("WEARABLE LISTENER", "In wearable listener, onCapabilityChanged")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.v("WEARABLE LISTENER", "In wearable listener, onMessagedReceived")
    }
}