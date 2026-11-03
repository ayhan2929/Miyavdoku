package com.miyavdoku.domain

/**
 * Counts valid hidden-cat arrangements using only the visible board regions
 * and game rules. The stored answer in PuzzleLevel is deliberately ignored.
 */
object PuzzleSolver {
    /** Stops at [limit] so uniqueness checks do not enumerate every solution. */
    fun countSolutions(level: PuzzleLevel, limit: Int = 2): Int {
        require(limit > 0)
        val size = level.size
        val usedColumns = BooleanArray(size)
        val usedRegions = HashSet<Int>()
        val chosenColumns = IntArray(size)
        var count = 0

        fun search(row: Int) {
            if (count >= limit) return
            if (row == size) {
                count++
                return
            }

            for (column in 0 until size) {
                if (usedColumns[column]) continue
                val cell = Cell(row, column)
                val region = level.regionAt(cell)
                if (region in usedRegions) continue

                // Cats in consecutive rows cannot share or neighbor columns.
                if (row > 0 && kotlin.math.abs(chosenColumns[row - 1] - column) <= 1) continue

                usedColumns[column] = true
                usedRegions.add(region)
                chosenColumns[row] = column
                search(row + 1)
                usedColumns[column] = false
                usedRegions.remove(region)
                if (count >= limit) return
            }
        }

        search(0)
        return count
    }

    fun hasUniqueSolution(level: PuzzleLevel): Boolean = countSolutions(level, limit = 2) == 1
}
