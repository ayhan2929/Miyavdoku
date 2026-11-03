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
        val wrongCells = (0 until level.size).flatMap { row ->
            (0 until level.size).map { column -> Cell(row, column) }
        }.filter { it !in level.catCells }.take(3)
        wrongCells.forEach { cell -> state = engine.revealCat(state, cell).state }
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
    @Test fun hintRequiresAPlayerDiscoveredCat() {
        val state = engine.newGame(level)
        val result = engine.useHint(state)
        assertEquals(MoveError.NO_HINT_AVAILABLE, result.error)
        assertEquals(state, result.state)
    }

    @Test fun hintMarksOnlyCellsRuledOutByFoundCatConstraints() {
        val cat = level.catCells.first()
        val state = engine.revealCat(engine.newGame(level), cat).state
        val result = engine.useHint(state)
        assertTrue(result.accepted)
        val hinted = requireNotNull(result.hintedCell)
        assertEquals(CellMark.EMPTY_MARK, result.state.markAt(hinted))
        assertTrue(
            hinted.row == cat.row ||
                hinted.column == cat.column ||
                level.regionAt(hinted) == level.regionAt(cat) ||
                (kotlin.math.abs(hinted.row - cat.row) <= 1 &&
                    kotlin.math.abs(hinted.column - cat.column) <= 1)
        )
        assertEquals(2, result.state.hintsRemaining)
        assertEquals(3, result.state.lives)
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

    @Test fun everyGeneratedCatalogLevelHasExactlyOneSolution() {
        LevelCatalog.levels.forEach { generated ->
            assertEquals(1, PuzzleSolver.countSolutions(generated, limit = 2))
        }
    }

    @Test fun generatedCatalogHasDistinctLayouts() {
        val layouts = LevelCatalog.levels.map { it.regions }.toSet()
        assertEquals(LevelCatalog.levels.size, layouts.size)
    }

    @Test fun generationIsDeterministicForSameSeed() {
        val first = LevelGenerator.generateCatalog(count = 5, seed = 42)
        val second = LevelGenerator.generateCatalog(count = 5, seed = 42)
        assertEquals(first.map { it.regions }, second.map { it.regions })
        assertEquals(first.map { it.catCells }, second.map { it.catCells })
    }

    @Test fun wrongGuessIsMarkedSoItCannotCostAnotherLife() {
        val state = engine.newGame(level)
        val wrong = (0 until level.size).flatMap { row ->
            (0 until level.size).map { column -> Cell(row, column) }
        }.first { it !in level.catCells }
        val first = engine.revealCat(state, wrong)
        assertEquals(CellMark.EMPTY_MARK, first.state.markAt(wrong))
        val second = engine.revealCat(first.state, wrong)
        assertEquals(2, second.state.lives)
        assertEquals(first.state, second.state)
        assertEquals(MoveError.ALREADY_MARKED, second.error)
    }

}
