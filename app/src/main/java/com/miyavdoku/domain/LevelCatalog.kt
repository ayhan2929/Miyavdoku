package com.miyavdoku.domain

/** Original built-in levels; puzzle data is independent from the UI. */
object LevelCatalog {
    private val baseColumns = listOf(0, 3, 1, 4, 2, 5)
    val levels: List<PuzzleLevel> = (1..30).map { id ->
        val size = 6
        val shift = (id - 1) % size
        val columns = baseColumns.map { (it + shift) % size }
        val cats = columns.mapIndexed { row, column -> Cell(row, column) }.toSet()
        val regions = List(size * size) { index ->
            val row = index / size
            val column = index % size
            (row + 2 * column + shift) % size
        }
        PuzzleLevel(
            id = id, size = size, regions = regions, catCells = cats,
            title = when (id) {
                1 -> "İlk Miyav"
                in 2..6 -> "Renkli Patiler"
                in 7..12 -> "Meraklı Kediler"
                in 13..18 -> "Pati Dedektifi"
                in 19..24 -> "Usta Miyav"
                else -> "Efsane Kedi"
            }
        )
    }
    fun level(id: Int): PuzzleLevel = levels.firstOrNull { it.id == id } ?: levels.first()
    fun nextAfter(id: Int): PuzzleLevel? = levels.firstOrNull { it.id == id + 1 }
}
