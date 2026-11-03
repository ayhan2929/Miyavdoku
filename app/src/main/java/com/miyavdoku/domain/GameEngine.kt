package com.miyavdoku.domain

import kotlin.random.Random

data class Cell(val row: Int, val column: Int)

data class Piece(
    val id: String,
    val cells: List<Cell>
) {
    init {
        require(id.isNotBlank()) { "Piece id cannot be blank." }
        require(cells.isNotEmpty()) { "A piece must contain at least one cell." }
        require(cells.all { it.row >= 0 && it.column >= 0 }) {
            "Piece coordinates cannot be negative."
        }
        require(cells.distinct().size == cells.size) { "Piece cells must be unique." }
    }

    val height: Int = cells.maxOf { it.row } + 1
    val width: Int = cells.maxOf { it.column } + 1
}

data class Board(
    val cells: List<Boolean> = List(BOARD_CELL_COUNT) { false }
) {
    init {
        require(cells.size == BOARD_CELL_COUNT) {
            "A MiyavDoku board must contain exactly 81 cells."
        }
    }

    operator fun get(row: Int, column: Int): Boolean {
        require(row in 0 until BOARD_SIZE && column in 0 until BOARD_SIZE)
        return cells[index(row, column)]
    }

    val occupiedCount: Int
        get() = cells.count { it }

    fun withOccupied(positions: Collection<Cell>): Board {
        val updated = cells.toMutableList()
        positions.forEach { position ->
            require(position.row in 0 until BOARD_SIZE)
            require(position.column in 0 until BOARD_SIZE)
            updated[index(position.row, position.column)] = true
        }
        return copy(cells = updated)
    }

    companion object {
        const val BOARD_SIZE = 9
        const val BOARD_CELL_COUNT = BOARD_SIZE * BOARD_SIZE

        private fun index(row: Int, column: Int) = row * BOARD_SIZE + column
    }
}

data class ClearSummary(
    val rows: Int = 0,
    val columns: Int = 0,
    val boxes: Int = 0,
    val clearedCells: Int = 0
) {
    val completedGroups: Int
        get() = rows + columns + boxes

    val hasClear: Boolean
        get() = clearedCells > 0
}

data class GameState(
    val board: Board = Board(),
    val availablePieces: List<Piece> = emptyList(),
    val score: Int = 0,
    val bestScore: Int = 0,
    val movesPlayed: Int = 0,
    val combo: Int = 0,
    val isGameOver: Boolean = false
) {
    init {
        require(score >= 0)
        require(bestScore >= score)
        require(movesPlayed >= 0)
        require(combo >= 0)
        require(availablePieces.map { it.id }.distinct().size == availablePieces.size)
    }
}

enum class MoveError {
    GAME_OVER,
    PIECE_NOT_AVAILABLE,
    INVALID_POSITION
}

data class MoveResult(
    val state: GameState,
    val error: MoveError? = null,
    val clearSummary: ClearSummary = ClearSummary(),
    val pointsEarned: Int = 0
) {
    val accepted: Boolean
        get() = error == null
}

/**
 * Pure Kotlin game rules. No Android, Compose, database or UI dependencies.
 *
 * A turn consumes exactly one tray piece. When the tray becomes empty,
 * a new set is generated. The game ends when no remaining piece can fit.
 */
class GameEngine(
    private val random: Random = Random.Default
) {
    fun newGame(bestScore: Int = 0): GameState {
        require(bestScore >= 0)
        val pieces = drawPieces()
        return GameState(
            availablePieces = pieces,
            bestScore = bestScore
        )
    }

    fun canPlace(board: Board, piece: Piece, origin: Cell): Boolean {
        if (origin.row < 0 || origin.column < 0) return false

        return piece.cells.all { cell ->
            val row = origin.row + cell.row
            val column = origin.column + cell.column
            row in 0 until Board.BOARD_SIZE &&
                column in 0 until Board.BOARD_SIZE &&
                !board[row, column]
        }
    }

    fun placePiece(
        state: GameState,
        pieceId: String,
        origin: Cell
    ): MoveResult {
        if (state.isGameOver) return MoveResult(state, MoveError.GAME_OVER)

        val piece = state.availablePieces.firstOrNull { it.id == pieceId }
            ?: return MoveResult(state, MoveError.PIECE_NOT_AVAILABLE)

        if (!canPlace(state.board, piece, origin)) {
            return MoveResult(state, MoveError.INVALID_POSITION)
        }

        val placedBoard = state.board.withOccupied(
            piece.cells.map { Cell(origin.row + it.row, origin.column + it.column) }
        )
        val (clearedBoard, summary) = clearCompletedGroups(placedBoard)

        val nextCombo = if (summary.hasClear) state.combo + 1 else 0
        val earned = calculatePoints(piece, summary, nextCombo)
        val nextScore = state.score + earned
        val remainingPieces = state.availablePieces.filterNot { it.id == piece.id }

        val nextPieces = if (remainingPieces.isEmpty()) drawPieces() else remainingPieces
        val gameOver = !hasAnyMove(clearedBoard, nextPieces)

        val nextState = state.copy(
            board = clearedBoard,
            availablePieces = nextPieces,
            score = nextScore,
            bestScore = maxOf(state.bestScore, nextScore),
            movesPlayed = state.movesPlayed + 1,
            combo = nextCombo,
            isGameOver = gameOver
        )

        return MoveResult(
            state = nextState,
            clearSummary = summary,
            pointsEarned = earned
        )
    }

    fun hasAnyMove(board: Board, pieces: List<Piece>): Boolean {
        if (pieces.isEmpty()) return false

        return pieces.any { piece ->
            if (piece.height > Board.BOARD_SIZE || piece.width > Board.BOARD_SIZE) {
                false
            } else {
                (0..Board.BOARD_SIZE - piece.height).any { row ->
                    (0..Board.BOARD_SIZE - piece.width).any { column ->
                        canPlace(board, piece, Cell(row, column))
                    }
                }
            }
        }
    }

    private fun clearCompletedGroups(board: Board): Pair<Board, ClearSummary> {
        val fullRows = (0 until Board.BOARD_SIZE).filter { row ->
            (0 until Board.BOARD_SIZE).all { column -> board[row, column] }
        }
        val fullColumns = (0 until Board.BOARD_SIZE).filter { column ->
            (0 until Board.BOARD_SIZE).all { row -> board[row, column] }
        }
        val fullBoxes = (0 until 3).flatMap { boxRow ->
            (0 until 3).mapNotNull { boxColumn ->
                val cells = (0 until 3).flatMap { localRow ->
                    (0 until 3).map { localColumn ->
                        Cell(boxRow * 3 + localRow, boxColumn * 3 + localColumn)
                    }
                }
                cells.takeIf { group ->
                    group.all { board[it.row, it.column] }
                }
            }
        }

        val cellsToClear = buildSet {
            fullRows.forEach { row ->
                (0 until Board.BOARD_SIZE).forEach { column ->
                    add(Cell(row, column))
                }
            }
            fullColumns.forEach { column ->
                (0 until Board.BOARD_SIZE).forEach { row ->
                    add(Cell(row, column))
                }
            }
            fullBoxes.flatten().forEach(::add)
        }

        val updated = board.cells.toMutableList()
        cellsToClear.forEach { cell ->
            updated[cell.row * Board.BOARD_SIZE + cell.column] = false
        }

        return Board(updated) to ClearSummary(
            rows = fullRows.size,
            columns = fullColumns.size,
            boxes = fullBoxes.size,
            clearedCells = cellsToClear.size
        )
    }

    private fun calculatePoints(
        piece: Piece,
        summary: ClearSummary,
        combo: Int
    ): Int {
        val placementPoints = piece.cells.size
        val clearPoints = summary.clearedCells * 2
        val groupBonus = summary.completedGroups * 5
        val comboBonus = if (summary.hasClear && combo > 1) (combo - 1) * 5 else 0
        return placementPoints + clearPoints + groupBonus + comboBonus
    }

    private fun drawPieces(): List<Piece> =
        PIECE_CATALOG.shuffled(random).take(PIECES_PER_HAND)

    companion object {
        const val PIECES_PER_HAND = 3

        val PIECE_CATALOG: List<Piece> = listOf(
            Piece("single", listOf(Cell(0, 0))),
            Piece("domino", listOf(Cell(0, 0), Cell(0, 1))),
            Piece("line-3", listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2))),
            Piece("line-4", listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2), Cell(0, 3))),
            Piece("corner-3", listOf(Cell(0, 0), Cell(1, 0), Cell(1, 1))),
            Piece("square-2", listOf(
                Cell(0, 0), Cell(0, 1), Cell(1, 0), Cell(1, 1)
            )),
            Piece("t-4", listOf(
                Cell(0, 0), Cell(0, 1), Cell(0, 2), Cell(1, 1)
            )),
            Piece("l-4", listOf(
                Cell(0, 0), Cell(1, 0), Cell(2, 0), Cell(2, 1)
            )),
            Piece("plus-5", listOf(
                Cell(0, 1), Cell(1, 0), Cell(1, 1), Cell(1, 2), Cell(2, 1)
            ))
        )

        // Kept temporarily so the first screen remains compatible during migration.
        val starterPieces: List<Piece> = PIECE_CATALOG.take(3)
    }
}
