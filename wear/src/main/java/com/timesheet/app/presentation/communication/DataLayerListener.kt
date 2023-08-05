package com.timesheet.app.presentation.communication

import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem

class DataLayerListener:
    DataClient.OnDataChangedListener
{

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        dataEventBuffer.forEach {
              when(it.type) {
                  DataEvent.TYPE_CHANGED -> onTypeChangeEvent(it)
                  else -> onDeleteEvent(it)
              }
        }
    }

    private fun onTypeChangeEvent(dataEvent: DataEvent) {
        val dataItem: DataItem = dataEvent.dataItem

        DataMapItem.fromDataItem(dataItem).dataMap.apply {
            Log.v("print", this.toString())
        }
    }

    private fun onDeleteEvent(dataEvent: DataEvent) {

    }
}