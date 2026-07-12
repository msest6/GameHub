package com.example.gamehub.tictactoe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController

/**
 * Prikazuje grid za igranje križić kružić igre protiv bota.
 * Minimax logika i sav state žive u TicTacToeBotViewModel -
 * ovaj composable samo prikazuje stanje i prosljeđuje dodire igrača.
 */
@Composable
fun TicTacToeBotGrid(navController: NavController, buttonColors: ButtonColors, viewModel: TicTacToeBotViewModel) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Text(
        text = "Na potezu: ${viewModel.currentTurn}",
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
        textAlign = TextAlign.Center,
        fontSize = (screenWidth.value * 0.08f).sp,
        color = MaterialTheme.colorScheme.tertiary
    )
    IconButton(
        onClick = { navController.navigate("tictactoe") },
        modifier = Modifier
            .padding(10.dp)
            .width(screenWidth / 5)
    ) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = "PvP",
            modifier = Modifier.size((screenWidth.value * 0.08f).dp)
        )
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0..2) {
                    val index = row * 3 + col
                    val isWinningCell = index in viewModel.winningLine

                    Button(
                        onClick = { viewModel.playMove(index) },
                        enabled = viewModel.board[index].isEmpty() && viewModel.winner == null,
                        modifier = Modifier
                            .padding(8.dp)
                            .width(screenWidth / 4)
                            .height(screenWidth / 4),
                        colors = buttonColors
                    ) {
                        Text(
                            text = viewModel.board[index],
                            color = if (isWinningCell) Color.Red else Color.Unspecified,
                            fontSize = (screenWidth.value * 0.1f).sp
                        )
                    }
                }
            }
        }
    }
    if (viewModel.winner != null) {
        Dialog(
            onDismissRequest = { /* sprječavamo zatvaranje klikom van */ }
        ) {
            AnimatedVisibility(
                visible = viewModel.winner != null,
                enter = fadeIn(animationSpec = tween(2000)) +
                        scaleIn(initialScale = 0.8f, animationSpec = tween(2000)),
                exit = fadeOut() + scaleOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    tonalElevation = 8.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = if (viewModel.winner == "draw") "Neriješeno" else "Pobjednik: ${viewModel.winner}",
                            fontSize = (screenWidth.value * 0.08f).sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = { viewModel.reset() },
                            modifier = Modifier
                                .width(screenWidth / 2)
                                .height(screenWidth / 6),
                            colors = buttonColors
                        ) {
                            Text(text = "Restart", fontSize = (screenWidth.value * 0.06f).sp)
                        }
                    }
                }
            }
        }
    }
}