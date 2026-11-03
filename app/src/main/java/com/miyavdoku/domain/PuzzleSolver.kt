package com.miyavdoku.domain

import kotlin.math.abs

data class PuzzleSearchMetrics(
    val visitedNodes: Int,
    val candidateChecks: Int,
    val solutionCount: Int
)

/**
 * Counts valid hidden-cat arrangements using only visible regions and rules.
 * The stored answer in PuzzleLevel is deliberately ignored.
 */
object PuzzleSolver {
    fun countSolutions(level: PuzzleLevel, limit: Int = 2): Int =
        analyze(level, limit).solutionCount

    /**
     * Measures how much search is required to prove a board's solution.
     * This is a consistent puzzle-complexity proxy, not a claim about
     * every human player's solving time.
     */
    fun analyze(level: PuzzleLevel, limit: Int = 2): PuzzleSearchMetrics {
        require(limit > 0)
        val size = level.size
        val usedColumns = BooleanArray(size)
        val usedRegions = HashSet<Int>()
        val chosenColumns = IntArray(size)
        var solutions = 0
        var nodes = 0
        var checks = 0

        fun search(row: Int) {
            if (solutions >= limit) return
            nodes++
            if (row == size) {
                solutions++
                return
            }
            for (column in 0 until size) {
                checks++
                if (usedColumns[column]) continue
                val region = level.regionAt(Cell(row, column))
                if (region in usedRegions) continue
                if (row > 0 && abs(chosenColumns[row - 1] - column) <= 1) continue

                usedColumns[column] = true
                usedRegions.add(region)
                chosenColumns[row] = column
                search(row + 1)
                usedColumns[column] = false
                usedRegions.remove(region)
                if (solutions >= limit) return
            }
        }

        search(0)
        return PuzzleSearchMetrics(nodes, checks, solutions)
    }

    fun hasUniqueSolution(level: PuzzleLevel): Boolean = countSolutions(level, limit = 2) == 1
}
