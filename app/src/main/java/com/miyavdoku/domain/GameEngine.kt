package com.miyavdoku.domain

import kotlin.math.abs

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

    fun regionAt(cell: Cell): Int {
        require(cell.row in 0 until size && cell.column in 0 until size)
        return regions[cell.row * size + cell.column]
    }

    private fun isValidSolution(): Boolean {
        if (catCells.map { it.row }.toSet().size != size) return false
        if (catCells.map { it.column }.toSet().size != size) return false
        if (catCells.map(::regionAt).toSet().size != size) return false
        return catsDoNotTouch(catCells)
    }
}

data class GameState(
    val level: PuzzleLevel,
    val marks: List<CellMark> = List(level.size * level.size) { CellMark.UNKNOWN },
    val lives: Int = GameEngine.STARTING_LIVES,
    val score: Int = 0,
    val hintsRemaining: Int = GameEngine.STARTING_HINTS,
    val isWon: Boolean = false,
    val isLost: Boolean = false
) {
    init {
        require(marks.size == level.size * level.size)
        require(lives in 0..GameEngine.STARTING_LIVES)
        require(hintsRemaining in 0..GameEngine.STARTING_HINTS)
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

enum class MoveError { GAME_FINISHED, OUT_OF_BOUNDS, ALREADY_FOUND, NO_HINTS, NO_HINT_AVAILABLE }

data class MoveResult(
    val state: GameState,
    val error: MoveError? = null,
    val foundCat: Boolean = false,
    val hintedCell: Cell? = null
) {
    val accepted: Boolean get() = error == null
}

class GameEngine {
    fun newGame(level: PuzzleLevel = sampleLevel()): GameState = GameState(level)

    fun toggleMark(state: GameState, cell: Cell): MoveResult {
        val error = validateCell(state, cell)
        if (error != null) return MoveResult(state, error)
        if (state.markAt(cell) == CellMark.FOUND_CAT) return MoveResult(state, MoveError.ALREADY_FOUND)
        val marks = state.marks.toMutableList()
        val i = index(state.level, cell)
        marks[i] = if (marks[i] == CellMark.EMPTY_MARK) CellMark.UNKNOWN else CellMark.EMPTY_MARK
        return MoveResult(state.copy(marks = marks))
    }

    fun clearMark(state: GameState, cell: Cell): MoveResult {
        val error = validateCell(state, cell)
        if (error != null) return MoveResult(state, error)
        if (state.markAt(cell) == CellMark.FOUND_CAT) return MoveResult(state, MoveError.ALREADY_FOUND)
        val marks = state.marks.toMutableList()
        marks[index(state.level, cell)] = CellMark.UNKNOWN
        return MoveResult(state.copy(marks = marks))
    }

    fun revealCat(state: GameState, cell: Cell): MoveResult {
        val error = validateCell(state, cell)
        if (error != null) return MoveResult(state, error)
        if (state.markAt(cell) == CellMark.FOUND_CAT) return MoveResult(state, MoveError.ALREADY_FOUND)

        if (cell !in state.level.catCells) {
            val lives = (state.lives - 1).coerceAtLeast(0)
            return MoveResult(state.copy(lives = lives, isLost = lives == 0))
        }

        val marks = state.marks.toMutableList()
        marks[index(state.level, cell)] = CellMark.FOUND_CAT
        val won = marks.count { it == CellMark.FOUND_CAT } == state.totalCats
        return MoveResult(
            state.copy(marks = marks, score = state.score + 100, isWon = won),
            foundCat = true
        )
    }

    /** Marks one logically impossible cell as empty without revealing a cat. */
    fun useHint(state: GameState): MoveResult {
        if (state.isFinished) return MoveResult(state, MoveError.GAME_FINISHED)
        if (state.hintsRemaining == 0) return MoveResult(state, MoveError.NO_HINTS)

        val found = state.level.catCells.filter { state.markAt(it) == CellMark.FOUND_CAT }
        val safeCell = (0 until state.level.size).asSequence().flatMap { row ->
            (0 until state.level.size).asSequence().map { column -> Cell(row, column) }
        }.firstOrNull { cell ->
            state.markAt(cell) == CellMark.UNKNOWN &&
                (found.any { it.row == cell.row || it.column == cell.column ||
                    state.level.regionAt(it) == state.level.regionAt(cell) ||
                    (abs(it.row - cell.row) <= 1 && abs(it.column - cell.column) <= 1) })
        } ?: return MoveResult(state, MoveError.NO_HINT_AVAILABLE)

        val marks = state.marks.toMutableList()
        marks[index(state.level, safeCell)] = CellMark.EMPTY_MARK
        return MoveResult(
            state.copy(marks = marks, hintsRemaining = state.hintsRemaining - 1),
            hintedCell = safeCell
        )
    }

    fun isValidSolution(level: PuzzleLevel): Boolean {
        val cats = level.catCells
        return cats.size == level.size &&
            cats.map { it.row }.toSet().size == level.size &&
            cats.map { it.column }.toSet().size == level.size &&
            cats.map(level::regionAt).toSet().size == level.size &&
            catsDoNotTouch(cats)
    }

    private fun validateCell(state: GameState, cell: Cell): MoveError? {
        if (state.isFinished) return MoveError.GAME_FINISHED
        if (cell.row !in 0 until state.level.size || cell.column !in 0 until state.level.size) {
            return MoveError.OUT_OF_BOUNDS
        }
        return null
    }

    private fun index(level: PuzzleLevel, cell: Cell) = cell.row * level.size + cell.column

    companion object {
        const val STARTING_LIVES = 3
        const val STARTING_HINTS = 3

        private fun catsDoNotTouch(cats: Set<Cell>): Boolean = cats.none { a ->
            cats.any { b -> a != b && abs(a.row - b.row) <= 1 && abs(a.column - b.column) <= 1 }
        }

        fun sampleLevel(): PuzzleLevel {
            val size = 6
            val solutionColumns = listOf(0, 3, 1, 4, 2, 5)
            val cats = solutionColumns.mapIndexed { row, column -> Cell(row, column) }.toSet()
            val regions = List(size * size) { index ->
                val row = index / size
                val column = index % size
                (row + 2 * column) % size
            }
            return PuzzleLevel(1, size, regions, cats, "İlk Miyav")
        }
    }
}
