package com.miyavdoku

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.playerProgressDataStore by preferencesDataStore(name = "player_progress")

data class PlayerProgress(
    val highestUnlockedLevel: Int = 1,
    val completedLevels: Set<Int> = emptySet(),
    val totalScore: Int = 0
)

class PlayerProgressStore(context: Context) {
    private val dataStore = context.applicationContext.playerProgressDataStore

    val progress: Flow<PlayerProgress> = dataStore.data.map { preferences ->
        PlayerProgress(
            highestUnlockedLevel = preferences[HIGHEST_UNLOCKED] ?: 1,
            completedLevels = preferences[COMPLETED_LEVELS].orEmpty()
                .mapNotNull(String::toIntOrNull).toSet(),
            totalScore = preferences[TOTAL_SCORE] ?: 0
        )
    }

    suspend fun completeLevel(levelId: Int, earnedScore: Int) {
        dataStore.edit { preferences ->
            val completed = preferences[COMPLETED_LEVELS].orEmpty().toMutableSet()
            val firstCompletion = completed.add(levelId.toString())
            preferences[COMPLETED_LEVELS] = completed
            preferences[HIGHEST_UNLOCKED] = maxOf(
                preferences[HIGHEST_UNLOCKED] ?: 1,
                (levelId + 1).coerceAtMost(30)
            )
            if (firstCompletion) {
                preferences[TOTAL_SCORE] = (preferences[TOTAL_SCORE] ?: 0) + earnedScore
            }
        }
    }

    private companion object {
        val HIGHEST_UNLOCKED = intPreferencesKey("highest_unlocked_level")
        val COMPLETED_LEVELS = stringSetPreferencesKey("completed_levels")
        val TOTAL_SCORE = intPreferencesKey("total_score")
    }
}
