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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
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
                    Text("🪙 1.250  +", modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), color = ink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("BÖLÜM ${game.level.id}/30", color = ink, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text("${game.level.title}  •  ${LevelCatalog.difficulty(game.level.id).tier.label}", color = ink.copy(alpha = .65f), fontSize = 12.sp)
                }
                Surface(color = Color.White, shape = RoundedCornerShape(16.dp)) {
                    Text("🐾  ${game.foundCats}/${game.totalCats}", modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp), color = purple, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Surface(color = Color(0xFFFFE4EA), shape = RoundedCornerShape(16.dp)) {
                    Text("♥ ".repeat(game.lives).trim(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp), color = Color(0xFFE65E80), fontWeight = FontWeight.Bold)
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
                                val bg = regionPalette[game.level.regionAt(cell) % regionPalette.size]
                                Box(Modifier.weight(1f).aspectRatio(1f).background(bg, RoundedCornerShape(9.dp))
                                    .border(if (selectedCell) 3.dp else 1.dp, if (selectedCell) purple else Color.White.copy(alpha = .9f), RoundedCornerShape(9.dp))
                                    .pointerInput(cell, game.isFinished) { detectTapGestures(onTap = { viewModel.tapCell(cell) }, onDoubleTap = { viewModel.doubleTapCell(cell) }) }, contentAlignment = Alignment.Center) {
                                    Text(when (mark) {
                                        CellMark.EMPTY_MARK -> "×"
                                        CellMark.FOUND_CAT -> "🐱"
                                        CellMark.UNKNOWN -> if (selectedCell) "•" else ""
                                    }, color = if (mark == CellMark.EMPTY_MARK) Color(0xFF8D75A0) else ink, fontSize = if (mark == CellMark.FOUND_CAT) 23.sp else 21.sp, fontWeight = FontWeight.Bold)
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
