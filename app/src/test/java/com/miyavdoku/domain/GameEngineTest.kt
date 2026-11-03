package com.miyavdoku.domain

import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {
    private val engine = GameEngine()
    private val level = GameEngine.sampleLevel()

    @Test fun sampleLevelHasOneCatPerRowColumnAndColor() {
        assertTrue(engine.isValidSolution(level))
        assertEquals(level.size, level.catCells.map { it.row }.toSet().size)
        assertEquals(level.size, level.catCells.map { it.column }.toSet().size)
        assertEquals(level.size, level.catCells.map(level::regionAt).toSet().size)
    }

    @Test fun catsNeverTouchEvenDiagonally() {
        val cats = level.catCells.toList()
        for (i in cats.indices) for (j in i + 1 until cats.size) {
            assertTrue(kotlin.math.abs(cats[i].row - cats[j].row) > 1 ||
                kotlin.math.abs(cats[i].column - cats[j].column) > 1)
        }
    }

    @Test fun newGameStartsWithThreeLivesAndEmptyMarks() {
        val state = engine.newGame(level)
        assertEquals(3, state.lives)
        assertEquals(0, state.foundCats)
        assertTrue(state.marks.all { it == CellMark.UNKNOWN })
    }

    @Test fun pencilMarkTogglesWithoutLosingLife() {
        val state = engine.newGame(level)
        val cell = Cell(0, 1)
        val marked = engine.toggleMark(state, cell).state
        assertEquals(CellMark.EMPTY_MARK, marked.markAt(cell))
        assertEquals(3, marked.lives)
        val cleared = engine.toggleMark(marked, cell).state
        assertEquals(CellMark.UNKNOWN, cleared.markAt(cell))
    }

    @Test fun correctGuessFindsCatAndAwardsPoints() {
        val state = engine.newGame(level)
        val cat = level.catCells.first()
        val result = engine.revealCat(state, cat)
        assertTrue(result.accepted)
        assertTrue(result.foundCat)
        assertEquals(CellMark.FOUND_CAT, result.state.markAt(cat))
        assertEquals(1, result.state.foundCats)
        assertEquals(100, result.state.score)
        assertEquals(3, result.state.lives)
    }

    @Test fun wrongGuessConsumesOneLife() {
        val state = engine.newGame(level)
        val wrong = (0 until level.size).flatMap { r ->
            (0 until level.size).map { c -> Cell(r, c) }
        }.first { it !in level.catCells }
        val result = engine.revealCat(state, wrong)
        assertEquals(2, result.state.lives)
        assertEquals(0, result.state.foundCats)
        assertFalse(result.state.isLost)
    }

    @Test fun thirdWrongGuessEndsGame() {
        var state = engine.newGame(level)
        val wrong = (0 until level.size).flatMap { r ->
            (0 until level.size).map { c -> Cell(r, c) }
        }.first { it !in level.catCells }
        repeat(3) { state = engine.revealCat(state, wrong).state }
        assertEquals(0, state.lives)
        assertTrue(state.isLost)
    }

    @Test fun findingAllCatsWinsLevel() {
        var state = engine.newGame(level)
        level.catCells.forEach { state = engine.revealCat(state, it).state }
        assertTrue(state.isWon)
        assertEquals(level.size, state.foundCats)
        assertEquals(level.size * 100, state.score)
    }

    @Test fun finishedGameRejectsFurtherMoves() {
        var state = engine.newGame(level)
        level.catCells.forEach { state = engine.revealCat(state, it).state }
        val result = engine.toggleMark(state, Cell(0, 0))
        assertEquals(MoveError.GAME_FINISHED, result.error)
        assertEquals(state, result.state)
    }

    @Test fun outOfBoundsCellIsRejected() {
        val state = engine.newGame(level)
        val result = engine.revealCat(state, Cell(6, 0))
        assertEquals(MoveError.OUT_OF_BOUNDS, result.error)
        assertEquals(state, result.state)
    }
    @Test fun catalogContainsThirtyValidLevels() {
        assertEquals(30, LevelCatalog.levels.size)
        LevelCatalog.levels.forEach { assertTrue(engine.isValidSolution(it)) }
    }

    @Test fun everyCatalogLevelHasSixCatsAndSixRegions() {
        LevelCatalog.levels.forEach { level ->
            assertEquals(6, level.catCells.size)
            assertEquals(6, level.catCells.map(level::regionAt).toSet().size)
        }
    }

}
