package com.miyavdoku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerProgressTest {
    @Test
    fun onlyFirstLevelIsUnlockedInitially() {
        val progress = PlayerProgress()
        assertTrue(progress.isUnlocked(1))
        assertFalse(progress.isUnlocked(2))
    }

    @Test
    fun completingLevelUnlocksNextLevelAndAddsScore() {
        val progress = PlayerProgress().completeLevel(1, 600)
        assertEquals(2, progress.highestUnlockedLevel)
        assertEquals(setOf(1), progress.completedLevels)
        assertEquals(600, progress.totalScore)
        assertTrue(progress.isUnlocked(2))
        assertFalse(progress.isUnlocked(3))
    }

    @Test
    fun replayingCompletedLevelDoesNotDuplicateScore() {
        val first = PlayerProgress().completeLevel(1, 600)
        val replay = first.completeLevel(1, 600)
        assertEquals(first, replay)
    }

    @Test
    fun completingLaterLevelKeepsPreviouslyUnlockedProgress() {
        val progress = PlayerProgress(highestUnlockedLevel = 8, completedLevels = setOf(1, 2), totalScore = 900)
            .completeLevel(5, 400)
        assertEquals(8, progress.highestUnlockedLevel)
        assertEquals(setOf(1, 2, 5), progress.completedLevels)
        assertEquals(1300, progress.totalScore)
    }

    @Test
    fun finalLevelDoesNotUnlockBeyondCatalog() {
        val progress = PlayerProgress(highestUnlockedLevel = 30).completeLevel(30, 1000)
        assertEquals(30, progress.highestUnlockedLevel)
    }
}
