package com.miyavdoku.domain

enum class PuzzleDifficulty(val label: String) {
    EASY("Kolay"),
    MEDIUM("Orta"),
    HARD("Zor"),
    EXPERT("Uzman")
}

data class LevelDifficulty(
    val levelId: Int,
    val tier: PuzzleDifficulty,
    val score: Int,
    val searchNodes: Int,
    val candidateChecks: Int
)

/**
 * Rates generated levels using the solver's deterministic search effort.
 * The catalog is then ordered into four balanced progression bands.
 */
object PuzzleDifficultyAnalyzer {
    fun analyze(level: PuzzleLevel): LevelDifficulty {
        val metrics = PuzzleSolver.analyze(level, limit = 2)
        val score = metrics.visitedNodes * 10 + metrics.candidateChecks
        return LevelDifficulty(
            levelId = level.id,
            tier = tierForScore(score),
            score = score,
            searchNodes = metrics.visitedNodes,
            candidateChecks = metrics.candidateChecks
        )
    }

    fun buildProgression(levels: List<PuzzleLevel>): Map<Int, LevelDifficulty> {
        require(levels.isNotEmpty())
        require(levels.map { it.id }.toSet().size == levels.size)

        val measured = levels.map { level ->
            val metrics = PuzzleSolver.analyze(level, limit = 2)
            val score = metrics.visitedNodes * 10 + metrics.candidateChecks
            LevelDifficulty(
                levelId = level.id,
                tier = PuzzleDifficulty.EASY,
                score = score,
                searchNodes = metrics.visitedNodes,
                candidateChecks = metrics.candidateChecks
            )
        }.sortedWith(compareBy<LevelDifficulty> { it.score }.thenBy { it.levelId })

        return measured.mapIndexed { index, rating ->
            val band = (index * 4 / measured.size).coerceAtMost(3)
            rating.levelId to rating.copy(tier = PuzzleDifficulty.entries[band])
        }.toMap()
    }

    private fun tierForScore(score: Int): PuzzleDifficulty = when {
        score < 250 -> PuzzleDifficulty.EASY
        score < 600 -> PuzzleDifficulty.MEDIUM
        score < 1_200 -> PuzzleDifficulty.HARD
        else -> PuzzleDifficulty.EXPERT
    }
}
