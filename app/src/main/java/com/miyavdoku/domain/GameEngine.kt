package com.miyavdoku.domain

/**
 * Pawdoku-inspired logic puzzle engine.
 * A level has one hidden cat in every row, column and color region.
 * Cats may not touch, including diagonally.
 */
data class Cell(val row: Int, val column: Int)

enum class CellMark { UNKNOWN, EMPTY_MARK, FOUND_CAT }

data class PuzzleLevel(
    val id: Int,
    val size: Int,
    val regions: List<Int>,
    val catCells: Set<Cell>,
    val title: String = "Seviye $id"
) {
    init {
        require(size >= 4)
        require(regions.size == size * size)
        require(catCells.size == size)
        require(catCells.all { it.row in 0 until size && it.column in 0 until size })
        require(isValidSolution())
    }

    fun regionAt(cell: Cell): Int = regions[cell.row * size + cell.column]

    private fun isValidSolution(): Boolean {
        if (catCells.map { it.row }.toSet().size != size) return false
        if (catCells.map { it.column }.toSet().size != size) return false
        if (catCells.map(::regionAt).toSet().size != size) return false
        return catCells.none { a ->
            catCells.any { b ->
                a != b && kotlin.math.abs(a.row - b.row) <= 1 &&
                    kotlin.math.abs(a.column - b.column) <= 1
            }
        }
    }
}

data class GameState(
    val level: PuzzleLevel,
    val marks: List<CellMark> = List(level.size * level.size) { CellMark.UNKNOWN },
    val lives: Int = STARTING_LIVES,
    val score: Int = 0,
    val isWon: Boolean = false,
    val isLost: Boolean = false
) {
    init {
        require(marks.size == level.size * level.size)
        require(lives in 0..STARTING_LIVES)
        require(score >= 0)
        require(!(isWon && isLost))
    }

    val foundCats: Int get() = marks.count { it == CellMark.FOUND_CAT }
    val totalCats: Int get() = level.size
    val isFinished: Boolean get() = isWon || isLost

    fun markAt(cell: Cell): CellMark {
        require(cell.row in 0 until level.size && cell.column in 0 until level.size)
        return marks[cell.row * level.size + cell.column]
    }
}

enum class MoveError { GAME_FINISHED, OUT_OF_BOUNDS, ALREADY_FOUND }

data class MoveResult(
    val state: GameState,
    val error: MoveError? = null,
    val foundCat: Boolean = false
) {
    val accepted: Boolean get() = error == null
}

class GameEngine {
    fun newGame(level: PuzzleLevel = sampleLevel()): GameState = GameState(level = level)

    /** A normal tap toggles a pencil mark, without consuming a life. */
    fun toggleMark(state: GameState, cell: Cell): MoveResult {
        if (state.isFinished) return MoveResult(state, MoveError.GAME_FINISHED)
        if (!isInside(state.level, cell)) return MoveResult(state, MoveError.OUT_OF_BOUNDS)
        if (state.markAt(cell) == CellMark.FOUND_CAT) {
            return MoveResult(state, MoveError.ALREADY_FOUND)
        }
        val updated = state.marks.toMutableList()
        val index = index(state.level, cell)
        updated[index] = if (updated[index] == CellMark.EMPTY_MARK) {
            CellMark.UNKNOWN
        } else {
            CellMark.EMPTY_MARK
        }
        return MoveResult(state.copy(marks = updated))
    }

    /** Explicitly checks the selected cell. A wrong guess costs one heart. */
    fun revealCat(state: GameState, cell: Cell): MoveResult {
        if (state.isFinished) return MoveResult(state, MoveError.GAME_FINISHED)
        if (!isInside(state.level, cell)) return MoveResult(state, MoveError.OUT_OF_BOUNDS)
        if (state.markAt(cell) == CellMark.FOUND_CAT) {
            return MoveResult(state, MoveError.ALREADY_FOUND)
        }

        if (cell !in state.level.catCells) {
            val remainingLives = (state.lives - 1).coerceAtLeast(0)
            return MoveResult(
                state.copy(lives = remainingLives, isLost = remainingLives == 0)
            )
        }

        val updated = state.marks.toMutableList()
        updated[index(state.level, cell)] = CellMark.FOUND_CAT
        val found = updated.count { it == CellMark.FOUND_CAT }
        val won = found == state.totalCats
        return MoveResult(
            state.copy(
                marks = updated,
                score = state.score + 100,
                isWon = won
            ),
            foundCat = true
        )
    }

    fun isValidSolution(level: PuzzleLevel): Boolean {
        val cats = level.catCells
        if (cats.size != level.size) return false
        if (cats.map { it.row }.toSet().size != level.size) return false
        if (cats.map { it.column }.toSet().size != level.size) return false
        if (cats.map(level::regionAt).toSet().size != level.size) return false
        return cats.none { a ->
            cats.any { b ->
                a != b && kotlin.math.abs(a.row - b.row) <= 1 &&
                    kotlin.math.abs(a.column - b.column) <= 1
            }
        }
    }

    private fun isInside(level: PuzzleLevel, cell: Cell) =
        cell.row in 0 until level.size && cell.column in 0 until level.size

    private fun index(level: PuzzleLevel, cell: Cell) = cell.row * level.size + cell.column

    companion object {
        const val STARTING_LIVES = 3

        /** Original sample puzzle; level data is separate from the rule engine. */
        fun sampleLevel(): PuzzleLevel {
            val size = 6
            val solutionColumns = listOf(0, 3, 1, 4, 2, 5)
            val cats = solutionColumns.mapIndexed { row, column -> Cell(row, column) }.toSet()
            val regions = List(size * size) { index ->
                val row = index / size
                val column = index % size
                (row + 2 * column) % size
            }
            return PuzzleLevel(
                id = 1,
                size = size,
                regions = regions,
                catCells = cats,
                title = "İlk Miyav"
            )
        }
    }
}
