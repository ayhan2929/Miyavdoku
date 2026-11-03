package com.miyavdoku.domain

data class Cell(val row: Int, val column: Int)
data class Piece(val id: String, val cells: List<Cell>)
data class Board(val cells: List<Boolean> = List(81) { false }) {
    operator fun get(row: Int, column: Int) = cells[row * 9 + column]
}
data class Turn(val board: Board, val cleared: Int, val points: Int)

/** Pure Kotlin rules: independent from Android and Compose. */
class GameEngine {
    fun canPlace(board: Board, piece: Piece, origin: Cell): Boolean =
        piece.cells.all {
            val row = origin.row + it.row
            val col = origin.column + it.column
            row in 0..8 && col in 0..8 && !board[row, col]
        }

    fun place(board: Board, piece: Piece, origin: Cell): Turn? {
        if (!canPlace(board, piece, origin)) return null
        val next = board.cells.toMutableList()
        piece.cells.forEach { next[(origin.row + it.row) * 9 + origin.column + it.column] = true }
        val clear = mutableSetOf<Cell>()
        for (r in 0..8) if ((0..8).all { c -> next[r * 9 + c] }) {
            for (c in 0..8) clear += Cell(r, c)
        }
        for (c in 0..8) if ((0..8).all { r -> next[r * 9 + c] }) {
            for (r in 0..8) clear += Cell(r, c)
        }
        for (br in 0..2) for (bc in 0..2) {
            val box = (0..2).flatMap { r -> (0..2).map { c -> Cell(br * 3 + r, bc * 3 + c) } }
            if (box.all { next[it.row * 9 + it.column] }) clear += box
        }
        clear.forEach { next[it.row * 9 + it.column] = false }
        return Turn(Board(next), clear.size, piece.cells.size + clear.size * 2)
    }

    companion object {
        val starterPieces = listOf(
            Piece("dot", listOf(Cell(0, 0))),
            Piece("line", listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2))),
            Piece("square", listOf(Cell(0, 0), Cell(0, 1), Cell(1, 0), Cell(1, 1)))
        )
    }
}
