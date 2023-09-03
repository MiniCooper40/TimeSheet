package com.timesheet.app.view.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MutableHistoricalStateFlow<T> {

    private val _current: MutableStateFlow<T?> = MutableStateFlow(null)

    var value: T? = null
        set(value) {
            field = value
            _current.value = value
        }

    private var previous: MutableMap<Int, Flow<T?>> = HashMap()
    fun hasHistoricalFlow(historicalKey: Int): Boolean = previous.containsKey(historicalKey)
    fun setHistoricalFlow(historicalKey: Int, historicalFlow: Flow<T?>) {
        previous[historicalKey] = historicalFlow
    }

    fun getHistoricalFlow(historicalKey: Int) = previous[historicalKey]

    fun toStateFlow(): HistoricalStateFlow<T> {
        return HistoricalStateFlow(
            _current.asStateFlow(),
            previous
        )
    }
}

data class HistoricalStateFlow<T> (
    val current: StateFlow<T?>,
    val previous: MutableMap<Int, Flow<T?>>
)