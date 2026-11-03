package com.miyavdoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miyavdoku.domain.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { GameScreen() } }
    }
}

@Composable
private fun GameScreen() {
    val engine = remember { GameEngine() }
    var game by remember { mutableStateOf(engine.newGame()) }
    var selected by remember { mutableStateOf<Piece?>(null) }
    var status by remember { mutableStateOf("Bir şekil seç, sonra tahtada bir hücreye dokun.") }

    Surface(Modifier.fillMaxSize(), color = Color(0xFFFFF7F0)) {
        Column(
            Modifier.fillMaxSize().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("MiyavDoku", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7651C9))
            Text("Kedili blok bulmaca", color = Color(0xFF8B7770))
            Spacer(Modifier.height(18.dp))
            Text("PUAN  ${game.score}     EN İYİ  ${game.bestScore}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF604B43))
            Spacer(Modifier.height(14.dp))
            Column(
                Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).padding(7.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                repeat(9) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(9) { column ->
                            Box(
                                Modifier.weight(1f).aspectRatio(1f)
                                    .background(
                                        if (game.board[row, column]) Color(0xFFFFA45B) else Color(0xFFF0E9E4),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable {
                                        val piece = selected
                                        if (piece == null) {
                                            status = "Önce bir şekil seç."
                                        } else {
                                            val result = engine.placePiece(game, piece.id, Cell(row, column))
                                            if (!result.accepted) {
                                                status = when (result.error) {
                                                    MoveError.GAME_OVER -> "Oyun bitti. Yeni oyun başlat."
                                                    MoveError.PIECE_NOT_AVAILABLE -> "Bu şekil artık elinde değil."
                                                    else -> "Bu şekil bu konuma yerleşmiyor."
                                                }
                                            } else {
                                                game = result.state
                                                selected = null
                                                status = when {
                                                    game.isGameOver -> "Oyun bitti! Puan: ${game.score}"
                                                    result.clearSummary.hasClear -> "${result.clearSummary.clearedCells} hücre temizlendi!"
                                                    else -> "Güzel hamle!"
                                                }
                                            }
                                        }
                                    }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(status, color = Color(0xFF6D5C54), fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            Text("ŞEKİL SEÇ", fontWeight = FontWeight.Bold, color = Color(0xFF7651C9))
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                game.availablePieces.forEach { piece ->
                    val label = when (piece.id) {
                        "single" -> "Tek"
                        "domino" -> "İkili"
                        "line-3" -> "Çizgi"
                        "line-4" -> "Uzun"
                        "corner-3" -> "Köşe"
                        "square-2" -> "Kare"
                        "t-4" -> "T"
                        "l-4" -> "L"
                        else -> "+"
                    }
                    Button(
                        onClick = { selected = piece; status = "$label şekli seçildi." },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected?.id == piece.id) Color(0xFF7651C9) else Color(0xFFFFA45B)
                        )
                    ) { Text(label) }
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = {
                game = engine.newGame(bestScore = game.bestScore)
                selected = null
                status = "Yeni oyun başladı."
            }) { Text("Yeni Oyun") }
            if (game.isGameOver) {
                Text("En iyi puan: ${game.bestScore}", fontWeight = FontWeight.Bold, color = Color(0xFF7651C9))
            }
        }
    }
}
