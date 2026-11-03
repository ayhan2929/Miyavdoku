package com.miyavdoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miyavdoku.domain.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gameViewModel = ViewModelProvider(this)[GameViewModel::class.java]
        setContent { MaterialTheme { MiyavDokuScreen(gameViewModel) } }
    }
}

private val ink = Color(0xFF594A60)
private val purple = Color(0xFF8560CF)
private val pink = Color(0xFFFF8EAE)
private val paper = Color(0xFFFFF8F3)
private val regionPalette = listOf(Color(0xFFFFDCE8), Color(0xFFDCEBFF), Color(0xFFDDF4D9), Color(0xFFFFEDC9), Color(0xFFE9DFFF), Color(0xFFD5F2EF))

@Composable
private fun MiyavDokuScreen(viewModel: GameViewModel) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val game = ui.game
    var showLevelPicker by remember { mutableStateOf(false) }
    var showResultDialog by remember(game.level.id, game.isFinished) { mutableStateOf(game.isFinished) }
    if (showLevelPicker) {
        AlertDialog(
            onDismissRequest = { showLevelPicker = false },
            title = { Text("BÖLÜM SEÇ", color = purple, fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState())) {
                    PuzzleDifficulty.entries.forEach { tier ->
                        Text(tier.label.uppercase(), color = ink, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                        val tierLevels = LevelCatalog.levels.filter { LevelCatalog.difficulty(it.id).tier == tier }
                        tierLevels.chunked(5).forEach { rowLevels ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                rowLevels.forEach { level ->
                                    val current = level.id == game.level.id
                                    val unlocked = level.id <= ui.progress.highestUnlockedLevel
                                    val completed = level.id in ui.progress.completedLevels
                                    Button(
                                        onClick = {
                                            viewModel.selectLevel(level.id)
                                            showLevelPicker = false
                                        },
                                        enabled = unlocked,
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = if (current) purple else Color(0xFFEDE5FA))
                                    ) {
                                        Text(
                                            when {
                                                completed -> "✓ ${level.id}"
                                                !unlocked -> "🔒"
                                                else -> level.id.toString()
                                            },
                                            color = if (current) Color.White else ink,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                repeat(5 - rowLevels.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLevelPicker = false }) { Text("KAPAT") } },
            containerColor = paper
        )
    }
    if (game.isFinished && showResultDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    if (game.isWon) "MİYAV! BÖLÜM TAMAMLANDI" else "CANLAR TÜKENDİ",
                    color = if (game.isWon) purple else Color(0xFFE65E80),
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Text(
                    if (game.isWon) "Tüm kedileri buldun! ${game.score} puan kazandın."
                    else "Bu bölümdeki üç canını da kullandın. Aynı bölümü yeniden deneyebilirsin.",
                    color = ink
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (game.isWon) viewModel.goToNextLevel() else viewModel.restartLevel()
                }) {
                    Text(
                        when {
                            !game.isWon -> "TEKRAR DENE"
                            game.level.id < 30 -> "SONRAKİ BÖLÜM"
                            else -> "TAMAM"
                        },
                        color = purple,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showResultDialog = false
                    showLevelPicker = true
                }) {
                    Text("BÖLÜMLER", color = ink)
                }
            },
            containerColor = paper
        )
    }
    Surface(Modifier.fillMaxSize(), color = paper) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("🐾", color = purple, fontSize = 25.sp)
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🐱 MiyavDoku", color = purple, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                    Text("MANTIKLA BUL, MİYAVLA KAZAN", color = ink.copy(alpha = .65f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(Modifier.weight(1f))
                Surface(color = Color(0xFFFFE8B6), shape = RoundedCornerShape(18.dp)) {
                    Text("⭐  ${ui.progress.totalScore}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), color = ink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("BÖLÜM ${game.level.id}/30", color = ink, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text("${game.level.title}  •  ${LevelCatalog.difficulty(game.level.id).tier.label}", color = ink.copy(alpha = .65f), fontSize = 12.sp)
                    Text(
                        "Açık: ${ui.progress.highestUnlockedLevel}  •  Tamamlanan: ${ui.progress.completedLevels.size}",
                        color = purple, fontSize = 10.sp, fontWeight = FontWeight.Bold
                    )
                }
                Surface(color = Color.White, shape = RoundedCornerShape(16.dp)) {
                    Text("🐾  ${game.foundCats}/${game.totalCats}", modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp), color = purple, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Surface(color = Color(0xFFFFE4EA), shape = RoundedCornerShape(16.dp)) {
                    Text("♥ ".repeat(game.lives).trim(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp), color = Color(0xFFE65E80), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(6.dp))
                TextButton(onClick = { showLevelPicker = true }) {
                    Text("BÖLÜMLER", color = purple, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("KEDİLERİ BUL", color = ink, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Text("İPUCU  ${game.hintsRemaining}", color = purple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            Surface(Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(22.dp), shadowElevation = 3.dp) {
                Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        repeat(game.level.size) { Text("🐾", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp) }
                    }
                    repeat(game.level.size) { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(game.level.size) { col ->
                                val cell = Cell(row, col)
                                val mark = game.markAt(cell)
                                val selectedCell = ui.selectedCell == cell
                                val cellStateDescription = when (mark) {
                                    CellMark.EMPTY_MARK -> "Boş olduğu işaretlendi"
                                    CellMark.WRONG_GUESS -> "Yanlış tahmin"
                                    CellMark.FOUND_CAT -> "Kedi bulundu"
                                    CellMark.UNKNOWN -> "Henüz kontrol edilmedi"
                                }
                                val bg = if (mark == CellMark.WRONG_GUESS) Color(0xFFFF7777)
                                    else regionPalette[game.level.regionAt(cell) % regionPalette.size]
                                val wrongGuessScale = remember(cell) { Animatable(1f) }
                                LaunchedEffect(mark) {
                                    if (mark == CellMark.WRONG_GUESS) {
                                        wrongGuessScale.snapTo(1f)
                                        wrongGuessScale.animateTo(0.88f, animationSpec = tween(75))
                                        wrongGuessScale.animateTo(1.06f, animationSpec = tween(90))
                                        wrongGuessScale.animateTo(1f, animationSpec = tween(90))
                                    }
                                }
                                Box(Modifier.weight(1f).aspectRatio(1f).graphicsLayer {
                                    scaleX = wrongGuessScale.value
                                    scaleY = wrongGuessScale.value
                                }.background(bg, RoundedCornerShape(9.dp))
                                    .border(if (selectedCell) 3.dp else 1.dp, if (selectedCell) purple else Color.White.copy(alpha = .9f), RoundedCornerShape(9.dp))
                                    .semantics {
                                        contentDescription = "${row + 1}. satır, ${col + 1}. sütun"
                                        stateDescription = cellStateDescription
                                        role = Role.Button
                                        selected = selectedCell
                                    }
                                    .pointerInput(cell, game.isFinished) {
                                        detectTapGestures(
                                            onTap = { viewModel.tapCell(cell) },
                                            onLongPress = { viewModel.longPressCell(cell) }
                                        )
                                    }, contentAlignment = Alignment.Center) {
                                    Text(when (mark) {
                                        CellMark.EMPTY_MARK, CellMark.WRONG_GUESS -> "×"
                                        CellMark.FOUND_CAT -> "🐱"
                                        CellMark.UNKNOWN -> if (selectedCell) "•" else ""
                                    }, color = when (mark) {
                                        CellMark.WRONG_GUESS -> Color(0xFFB00020)
                                        CellMark.FOUND_CAT -> Color.Black
                                        CellMark.EMPTY_MARK -> Color(0xFF8D75A0)
                                        CellMark.UNKNOWN -> ink
                                    }, fontSize = if (mark == CellMark.FOUND_CAT) 23.sp else 21.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Surface(color = Color(0xFFF0E9FF), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("💡", fontSize = 22.sp)
                    Spacer(Modifier.width(9.dp))
                    Text(ui.message, color = ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(12.dp))
            Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(13.dp)) {
                    Text("OYUN KURALLARI", color = purple, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    Spacer(Modifier.height(7.dp))
                    Text("• Her satırda yalnızca bir kedi bulunur.", color = ink, fontSize = 12.sp)
                    Text("• Her sütunda ve renk bölgesinde bir kedi vardır.", color = ink, fontSize = 12.sp)
                    Text("• Kediler birbirine, çapraz dâhil, dokunamaz.", color = ink, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GameActionButton("🐾 KEDİ KOY", Modifier.weight(1.2f), purple, ui.selectedCell != null && !game.isFinished) { viewModel.revealCat() }
                GameActionButton("× İŞARETLE", Modifier.weight(1f), pink, ui.selectedCell != null && !game.isFinished) { viewModel.toggleMark() }
                GameActionButton("TEMİZLE", Modifier.weight(.85f), Color(0xFF8D9AAE), ui.selectedCell != null && !game.isFinished) { viewModel.clearMark() }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = viewModel::useHint, enabled = !game.isFinished && game.hintsRemaining > 0, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                    Text("İPUCU AL", color = purple, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = viewModel::restartLevel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                    Text("YENİDEN BAŞLAT", color = ink, fontWeight = FontWeight.Bold)
                }
            }
            if (game.isWon) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = viewModel::goToNextLevel, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Text(if (game.level.id < 30) "SONRAKİ BÖLÜM" else "TÜM BÖLÜMLER TAMAMLANDI")
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("İLERLEME  ${game.foundCats}/${game.totalCats}", color = ink.copy(alpha = .7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            LinearProgressIndicator(progress = { game.foundCats.toFloat() / game.totalCats }, modifier = Modifier.fillMaxWidth().height(8.dp), color = pink, trackColor = Color(0xFFF1E4E8))
            if (game.isFinished) {
                Spacer(Modifier.height(10.dp))
                Text(if (game.isWon) "BÖLÜM TAMAMLANDI • +${game.score} PUAN" else "BÖLÜM BAŞARISIZ", color = purple, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun GameActionButton(label: String, modifier: Modifier, color: Color, enabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier.height(48.dp), shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, disabledContainerColor = color.copy(alpha = .35f))) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, lineHeight = 12.sp)
    }
}
