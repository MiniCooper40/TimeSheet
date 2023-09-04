package com.timesheet.app.view.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.timesheet.app.application.MyApplication
import com.timesheet.app.data.repository.PreferenceKeys
import com.timesheet.app.data.repository.TimeSheetPreferencesRepository
import com.timesheet.app.data.repository.TimeTrackerRepository
import com.timesheet.app.data.repository.WeeklyStrategy
import kotlinx.coroutines.launch
import java.time.DayOfWeek

class PreferencesViewModel(
    private val timeSheetPreferencesRepository: TimeSheetPreferencesRepository
): ViewModel() {

    val preferences = timeSheetPreferencesRepository.preferencesFlow()

    fun setWeeklyCycleStartDay(dayOfWeek: DayOfWeek) {
        viewModelScope.launch {
            timeSheetPreferencesRepository.set(
                PreferenceKeys.weeklyCycleStartDayKey, dayOfWeek.value
            )
        }
    }

    fun setWeeklyCycleStrategy(strategy: WeeklyStrategy) {
        viewModelScope.launch {
            timeSheetPreferencesRepository.set(
                PreferenceKeys.weeklyStrategyKey, strategy.value
            )
        }
    }
    companion object {

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                // Create a SavedStateHandle for this ViewModel from extras
                val savedStateHandle = extras.createSavedStateHandle()

                return PreferencesViewModel(
                    TimeSheetPreferencesRepository(
                        application.applicationContext
                    )
                ) as T
            }
        }
    }
}