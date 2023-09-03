package com.timesheet.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.timesheet.app.ui.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class WeeklyStrategy(
    val value: String
) {
    TRAILING("TRAILING"),
    CYCLE("CYCLE")
}

object PreferenceKeys {
    val weeklyStrategyKey = stringPreferencesKey("weekly_strategy")
    val weeklyCycleStartDayKey = intPreferencesKey("weekly_cycle_start_day")
}
class TimeSheetPreferencesRepository(
    val context: Context
) {

    suspend fun getWeeklyStrategy(): WeeklyStrategy {
        val strategyValue = context.dataStore.data.firstOrNull()?.get(PreferenceKeys.weeklyStrategyKey) ?: WeeklyStrategy.TRAILING.value
        return WeeklyStrategy.values().firstOrNull { it.value == strategyValue } ?: WeeklyStrategy.TRAILING
    }

    suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit {
            it[key] = value
        }
    }

    fun preferencesFlow(): Flow<Preferences> {
        return context.dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                val weeklyStrategy = preferences[PreferenceKeys.weeklyStrategyKey] ?: WeeklyStrategy.TRAILING.value
                val weeklyCycleStartDay = preferences[PreferenceKeys.weeklyCycleStartDayKey] ?: 1

                val defaultedPreferences = preferences.toMutablePreferences()
                defaultedPreferences[PreferenceKeys.weeklyStrategyKey] = weeklyStrategy
                defaultedPreferences[PreferenceKeys.weeklyCycleStartDayKey] = weeklyCycleStartDay

                defaultedPreferences.toPreferences()
            }
    }

    suspend fun getWeeklyCycleStartDay(): DayOfWeek {
        val dayOfWeekValue = context.dataStore.data.firstOrNull()?.get(PreferenceKeys.weeklyCycleStartDayKey) ?: 1
        return DayOfWeek.of(dayOfWeekValue)
    }


}