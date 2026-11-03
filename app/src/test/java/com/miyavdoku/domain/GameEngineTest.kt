package com.miyavdoku.domain

import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {
    private val engine = GameEngine(Random(7))

    @Test
    fun newGameCreatesEmptyBoardAndThreeDistinctPieces() {
        val state = engine.newGame()
        assertEquals(0, state.board.occupiedCount)
        assertEquals(3, state.availablePieces.size)
        assertEquals(3, state.availablePieces.map { it.id }.distinct().size)
        assertFalse(state.isGameOver)
    }

    @Test
    fun rejectsOutOfBoundsPlacementWithoutChangingState() {
        val state = engine.newGame()
        val piece = state.availablePieces.first()
        val result = engine.placePiece(state, piece.id, Cell(9, 0))

        assertFalse(result.accepted)
        assertEquals(MoveError.INVALID_POSITION, result.error)
        assertEquals(state, result.state)
    }

    @Test
    fun rejectsPieceThatIsNotInTray() {
        val state = engine.newGame()
        val result = engine.placePiece(state, "unknown-piece", Cell(0, 0))

        assertEquals(MoveError.PIECE_NOT_AVAILABLE, result.error)
        assertEquals(state, result.state)
    }

    @Test
    fun successfulMoveConsumesExactlyOnePieceAndIncreasesScore() {
        val state = engine.newGame()
        val piece = state.availablePieces.first()
        val result = engine.placePiece(state, piece.id, Cell(0, 0))

        assertTrue(result.accepted)
        assertEquals(2, result.state.availablePieces.size)
        assertEquals(1, result.state.movesPlayed)
        assertEquals(piece.cells.size, result.pointsEarned)
        assertEquals(piece.cells.size, result.state.score)
    }

    @Test
    fun completingRowClearsIt() {
        val cells = MutableList(81) { false }
        for (column in 0..7) cells[column] = true
        val board = Board(cells)
        val single = GameEngine.PIECE_CATALOG.first { it.id == "single" }
        val result = engine.placePiece(
            GameState(board = board, availablePieces = listOf(single)),
            single.id,
            Cell(0, 8)
        )

        assertTrue(result.accepted)
        assertEquals(1, result.clearSummary.rows)
        assertEquals(9, result.clearSummary.clearedCells)
        assertTrue((0..8).none { result.state.board[0, it] })
    }

    @Test
    fun completingThreeByThreeBoxClearsIt() {
        val cells = MutableList(81) { false }
        for (row in 0..2) for (column in 0..1) {
            cells[row * 9 + column] = true
        }
        for (row in 0..1) cells[row * 9 + 2] = true

        val single = GameEngine.PIECE_CATALOG.first { it.id == "single" }
        val result = engine.placePiece(
            GameState(board = Board(cells), availablePieces = listOf(single)),
            single.id,
            Cell(2, 2)
        )

        assertEquals(1, result.clearSummary.boxes)
        assertEquals(9, result.clearSummary.clearedCells)
        for (row in 0..2) for (column in 0..2) {
            assertFalse(result.state.board[row, column])
        }
    }

    @Test
    fun fullBoardHasNoLegalPlacement() {
        val fullBoard = Board(List(81) { true })
        assertFalse(engine.hasAnyMove(fullBoard, GameEngine.PIECE_CATALOG))
    }

    @Test
    fun completedMoveRefillsAnEmptyTrayWithThreeNewPieces() {
        val single = GameEngine.PIECE_CATALOG.first { it.id == "single" }
        val state = GameState(availablePieces = listOf(single))
        val result = engine.placePiece(state, single.id, Cell(0, 0))

        assertTrue(result.accepted)
        assertEquals(3, result.state.availablePieces.size)
        assertEquals(1, result.state.movesPlayed)
    }

    @Test
    fun gameOverStateRejectsFurtherMoves() {
        val single = GameEngine.PIECE_CATALOG.first { it.id == "single" }
        val state = GameState(availablePieces = listOf(single), isGameOver = true)
        val result = engine.placePiece(state, single.id, Cell(0, 0))

        assertEquals(MoveError.GAME_OVER, result.error)
        assertEquals(state, result.state)
    }

    @Test
    fun bestScoreNeverDecreases() {
        val state = engine.newGame(bestScore = 500)
        val piece = state.availablePieces.first()
        val result = engine.placePiece(state, piece.id, Cell(0, 0))

        assertEquals(500, result.state.bestScore)
    }
}
