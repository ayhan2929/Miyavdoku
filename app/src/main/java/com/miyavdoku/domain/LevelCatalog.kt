package com.miyavdoku.domain

/**
 * Temporary generated catalog. The final level count will be decided later.
 * Levels are ranked by measured solver complexity and grouped into four
 * balanced difficulty bands.
 */
object LevelCatalog {
    const val TEMPORARY_LEVEL_COUNT = 30
    val levels: List<PuzzleLevel> = LevelGenerator.generateCatalog(
        count = TEMPORARY_LEVEL_COUNT,
        seed = 20261103
    )
    val difficultyRatings: Map<Int, LevelDifficulty> =
        PuzzleDifficultyAnalyzer.buildProgression(levels)

    fun level(id: Int): PuzzleLevel = levels.firstOrNull { it.id == id } ?: levels.first()
    fun difficulty(id: Int): LevelDifficulty =
        difficultyRatings[id] ?: PuzzleDifficultyAnalyzer.analyze(level(id))
    fun nextAfter(id: Int): PuzzleLevel? = levels.firstOrNull { it.id == id + 1 }
}
