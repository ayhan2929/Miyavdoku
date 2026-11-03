package com.miyavdoku.domain

import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {
    private val engine = GameEngine()
    @Test fun rejectsOutOfBoundsPlacement() {
        assertFalse(engine.canPlace(Board(), GameEngine.starterPieces[0], Cell(9, 0)))
    }
    @Test fun rejectsOccupiedCell() {
        val board = Board().copy(cells = List(81) { it == 10 })
        assertFalse(engine.canPlace(board, GameEngine.starterPieces[0], Cell(1, 1)))
    }
    @Test fun clearsCompletedRow() {
        val cells = MutableList(81) { false }
        for (column in 0..7) cells[column] = true
        val turn = engine.place(Board(cells), GameEngine.starterPieces[0], Cell(0, 8))!!
        assertEquals(9, turn.cleared)
        assertTrue((0..8).none { turn.board[0, it] })
    }
}
