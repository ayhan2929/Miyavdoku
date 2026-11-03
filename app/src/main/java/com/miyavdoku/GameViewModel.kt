package com.miyavdoku

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.miyavdoku.domain.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GameUiState(
    val game: GameState,
    val selectedCell: Cell? = null,
    val message: String = "Gizli kedileri bulmak için ipuçlarını takip et!",
    val progress: PlayerProgress = PlayerProgress()
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val progressStore = PlayerProgressStore(application)
    init {
        viewModelScope.launch {
            progressStore.progress.collect { progress ->
                _uiState.update { current ->
                    val restoredLevel = LevelCatalog.level(progress.lastPlayedLevel)
                    if (current.progress == PlayerProgress() && current.game.level.id == 1 && restoredLevel.id != 1) {
                        current.copy(game = engine.newGame(restoredLevel), progress = progress,
                            message = "Kaldığın bölüm yeniden açıldı. İyi eğlenceler!")
                    } else current.copy(progress = progress)
                }
            }
        }
    }
    private val engine = GameEngine()
    private val _uiState = MutableStateFlow(GameUiState(engine.newGame(LevelCatalog.level(1))))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun selectCell(cell: Cell) {
        _uiState.update { current ->
            if (current.game.isFinished) current
            else current.copy(selectedCell = cell, message = "Seçili hücre: ${cell.row + 1}. satır, ${cell.column + 1}. sütun")
        }
    }

    fun tapCell(cell: Cell) {
        _uiState.update { current ->
            if (current.game.isFinished) current
            else {
                val selected = current.copy(selectedCell = cell)
                applyResult(selected, engine.toggleMark(current.game, cell), "Hücre boş olarak işaretlendi.")
            }
        }
    }

    fun doubleTapCell(cell: Cell) {
        _uiState.update { current ->
            if (current.game.isFinished) current
            else {
                val selected = current.copy(selectedCell = cell)
                applyResult(selected, engine.revealCat(current.game, cell), "Hücre kontrol edildi.")
            }
        }
    }

    fun toggleMark() = act("Hücre işaretlendi.") { state, cell -> engine.toggleMark(state, cell) }
    fun clearMark() = act("İşaret temizlendi.") { state, cell -> engine.clearMark(state, cell) }
    fun revealCat() = act("Hücre kontrol edildi.") { state, cell -> engine.revealCat(state, cell) }

    fun useHint() {
        _uiState.update { current -> applyResult(current, engine.useHint(current.game), "İpucu kullanıldı.") }
    }

    fun restartLevel() {
        _uiState.update { current ->
            current.copy(game = engine.newGame(current.game.level), selectedCell = null, message = "Bölüm yeniden başladı. Bol şans!")
        }
    }

    fun selectLevel(levelId: Int) {
        val selected = LevelCatalog.levels.firstOrNull { it.id == levelId } ?: return
        if (!_uiState.value.progress.isUnlocked(levelId)) return
        viewModelScope.launch { progressStore.setLastPlayedLevel(levelId) }
        _uiState.update { GameUiState(engine.newGame(selected), message = "${selected.title} başladı. İyi eğlenceler!") }
    }

    fun goToNextLevel() {
        _uiState.update { current ->
            if (!current.game.isWon) current
            else {
                val next = LevelCatalog.nextAfter(current.game.level.id)
                if (next == null) current.copy(message = "Tebrikler! Tüm mevcut bölümleri tamamladın.")
                else {
                    viewModelScope.launch { progressStore.setLastPlayedLevel(next.id) }
                    GameUiState(engine.newGame(next), message = "Yeni bölüm başladı. İyi eğlenceler!")
                }
            }
        }
    }

    private fun act(success: String, operation: (GameState, Cell) -> MoveResult) {
        _uiState.update { current ->
            val cell = current.selectedCell ?: return@update current.copy(message = "Önce tahtadan bir hücre seç.")
            applyResult(current, operation(current.game, cell), success)
        }
    }

    private fun applyResult(current: GameUiState, result: MoveResult, success: String): GameUiState {
        if (!result.accepted) {
            val message = when (result.error) {
                MoveError.NO_HINTS -> "İpucu hakkın kalmadı."
                MoveError.NO_HINT_AVAILABLE -> "Şu anda kullanılabilir ipucu yok."
                MoveError.ALREADY_FOUND -> "Bu kediyi zaten buldun."
                MoveError.GAME_FINISHED -> "Bu bölüm tamamlandı."
                else -> "Bu hücrede işlem yapılamıyor."
            }
            return current.copy(message = message)
        }
        val game = result.state
        if (game.isWon && !current.game.isWon) {
            viewModelScope.launch { progressStore.completeLevel(game.level.id, game.score) }
        }
        val message = when {
            game.isWon -> "MİYAV! Bölümü tamamladın!"
            game.isLost -> "Canların bitti. Yeniden deneyebilirsin."
            result.foundCat -> "Harika! Bir kedi buldun."
            result.hintedCell != null -> "İpucu: Güvenli bir hücre işaretlendi."
            else -> success
        }
        return current.copy(game = game, message = message)
    }
}
