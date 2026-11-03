package com.miyavdoku.domain

import kotlin.random.Random

/**
 * Generates original boards and accepts only layouts with exactly one
 * rule-valid solution. The generated answer is not used by PuzzleSolver.
 */
object LevelGenerator {
    fun generateCatalog(
        count: Int,
        seed: Int = 20261103,
        size: Int = 6,
        maxAttemptsPerLevel: Int = 20_000
    ): List<PuzzleLevel> {
        require(count > 0)
        require(size >= 4)
        require(maxAttemptsPerLevel > 0)

        val random = Random(seed)
        val layouts = HashSet<List<Int>>()
        val result = ArrayList<PuzzleLevel>(count)

        for (id in 1..count) {
            var accepted: PuzzleLevel? = null
            for (attempt in 0 until maxAttemptsPerLevel) {
                if (accepted != null) break
                val columns = (0 until size).shuffled(random)
                if ((1 until size).any { row ->
                        kotlin.math.abs(columns[row] - columns[row - 1]) <= 1
                    }) continue

                val cats = columns.mapIndexed { row, column -> Cell(row, column) }.toSet()
                val regions = MutableList(size * size) { random.nextInt(size) }
                cats.forEach { cell -> regions[cell.row * size + cell.column] = cell.row }
                val immutableRegions = regions.toList()

                if (immutableRegions in layouts) return@repeat

                val candidate = PuzzleLevel(
                    id = id,
                    size = size,
                    regions = immutableRegions,
                    catCells = cats,
                    title = titleFor(id)
                )
                if (PuzzleSolver.hasUniqueSolution(candidate)) {
                    layouts.add(immutableRegions)
                    accepted = candidate
                }
            }
            checkNotNull(accepted) {
                "Could not generate a unique level $id after $maxAttemptsPerLevel attempts"
            }
            result.add(accepted)
        }
        return result
    }

    private fun titleFor(id: Int): String = when (id) {
        1 -> "İlk Miyav"
        in 2..6 -> "Renkli Patiler"
        in 7..12 -> "Meraklı Kediler"
        in 13..18 -> "Pati Dedektifi"
        in 19..24 -> "Usta Miyav"
        else -> "Efsane Kedi"
    }
}
