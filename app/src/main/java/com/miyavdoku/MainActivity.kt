package com.miyavdoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

private val regionColors = listOf(
    Color(0xFFFFD6E7), Color(0xFFD9E8FF), Color(0xFFDDF5D8),
    Color(0xFFFFE8B8), Color(0xFFE9DDFB), Color(0xFFCFF2EF)
)

@Composable
private fun GameScreen() {
    val engine = remember { GameEngine() }
    var game by remember { mutableStateOf(engine.newGame()) }
    var selected by remember { mutableStateOf<Cell?>(null) }
    var status by remember { mutableStateOf("Renk, satır ve sütun kurallarını kullanarak kedileri bul.") }

    fun update(result: MoveResult, success: String, failure: String) {
        if (!result.accepted) {
            status = failure
        } else {
            game = result.state
            status = when {
                game.isWon -> "Harika! Bölüm tamamlandı."
                game.isLost -> "Canların bitti. Tekrar deneyebilirsin."
                result.foundCat -> "Miyav! Bir kedi buldun."
                else -> success
            }
        }
    }

    Surface(Modifier.fillMaxSize(), color = Color(0xFFFFF8F2)) {
        Column(
            Modifier.fillMaxSize().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("MiyavDoku", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7651C9))
            Text("Gizli kedileri mantıkla bul", color = Color(0xFF8B7770))
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("BÖLÜM  ${game.level.id}", fontWeight = FontWeight.Bold, color = Color(0xFF604B43))
                Text("BULUNAN  ${game.foundCats}/${game.totalCats}", fontWeight = FontWeight.Bold, color = Color(0xFF604B43))
                Text("CAN  ${"♥".repeat(game.lives)}", color = Color(0xFFE45D78), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(18.dp)).padding(7.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                repeat(game.level.size) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(game.level.size) { column ->
                            val cell = Cell(row, column)
                            val mark = game.markAt(cell)
                            val base = regionColors[game.level.regionAt(cell) % regionColors.size]
                            Box(
                                Modifier.weight(1f).aspectRatio(1f)
                                    .background(base, RoundedCornerShape(7.dp))
                                    .border(
                                        if (selected == cell) 3.dp else 1.dp,
                                        if (selected == cell) Color(0xFF7651C9) else Color.White,
                                        RoundedCornerShape(7.dp)
                                    )
                                    .clickable {
                                        selected = cell
                                        if (!game.isFinished) {
                                            update(engine.toggleMark(game, cell), "Hücre işaretlendi.", "Bu hücre seçilemiyor.")
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    when (mark) {
                                        CellMark.EMPTY_MARK -> "×"
                                        CellMark.FOUND_CAT -> "🐱"
                                        CellMark.UNKNOWN -> ""
                                    },
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7651C9)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(status, color = Color(0xFF6D5C54), fontSize = 14.sp)
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    selected?.let { cell ->
                        update(engine.revealCat(game, cell), "Seçilen hücre kontrol edildi.", "Önce geçerli bir hücre seç.")
                    } ?: run { status = "Önce tahtadan bir hücre seç." }
                },
                enabled = selected != null && !game.isFinished,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7651C9)),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Seçili Hücrede Kedi Var") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    game = engine.newGame(game.level)
                    selected = null
                    status = "Bölüm yeniden başladı."
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Bölümü Yeniden Başlat") }
            if (game.isFinished) {
                Text(
                    if (game.isWon) "Bölüm tamamlandı • +${game.score} puan" else "Bölüm başarısız",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7651C9)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("İpucu: Her satırda, sütunda ve renkte bir kedi vardır. Kediler birbirine değmez.", fontSize = 12.sp, color = Color(0xFF8B7770))
        }
    }
}
