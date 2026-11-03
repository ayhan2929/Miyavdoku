package com.miyavdoku.domain

/**
 * Temporary generated catalog. The final level count and difficulty curve
 * will be defined separately; every current entry has a unique solution.
 */
object LevelCatalog {
    const val TEMPORARY_LEVEL_COUNT = 30
    val levels: List<PuzzleLevel> = LevelGenerator.generateCatalog(
        count = TEMPORARY_LEVEL_COUNT,
        seed = 20261103
    )

    fun level(id: Int): PuzzleLevel = levels.firstOrNull { it.id == id } ?: levels.first()
    fun nextAfter(id: Int): PuzzleLevel? = levels.firstOrNull { it.id == id + 1 }
}
