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
    var board by remember { mutableStateOf(Board()) }
    var score by remember { mutableIntStateOf(0) }
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
            Text("PUAN  $score", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF604B43))
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
                                        if (board[row, column]) Color(0xFFFFA45B) else Color(0xFFF0E9E4),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable {
                                        val piece = selected
                                        if (piece == null) {
                                            status = "Önce bir şekil seç."
                                        } else {
                                            val turn = engine.place(board, piece, Cell(row, column))
                                            if (turn == null) {
                                                status = "Bu şekil bu konuma yerleşmiyor."
                                            } else {
                                                board = turn.board
                                                score += turn.points
                                                selected = null
                                                status = if (turn.cleared > 0) "${turn.cleared} hücre temizlendi!" else "Güzel hamle!"
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
                GameEngine.starterPieces.forEach { piece ->
                    val label = when (piece.id) { "dot" -> "Tek"; "line" -> "Çizgi"; else -> "Kare" }
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
                board = Board()
                score = 0
                selected = null
                status = "Yeni oyun başladı."
            }) { Text("Yeni Oyun") }
        }
    }
}
