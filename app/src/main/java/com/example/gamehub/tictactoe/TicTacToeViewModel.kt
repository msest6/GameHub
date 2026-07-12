package com.example.gamehub.tictactoe

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

private val WINNING_COMBINATIONS = listOf(
    listOf(0, 1, 2),
    listOf(3, 4, 5),
    listOf(6, 7, 8),
    listOf(0, 3, 6),
    listOf(1, 4, 7),
    listOf(2, 5, 8),
    listOf(0, 4, 8),
    listOf(2, 4, 6)
)

/**
 * Drži cijeli state i logiku za križić-kružić igru protiv drugog igrača.
 * Composable (TicTacToeGrid) samo prikazuje ovo stanje i prosljeđuje dodire.
 */
class TicTacToeViewModel : ViewModel() {

    val board = mutableStateListOf("", "", "", "", "", "", "", "", "")

    var currentPlayer by mutableStateOf("X")
        private set

    /** "X", "O", "draw", ili null dok igra još traje. */
    var winner by mutableStateOf<String?>(null)
        private set

    var winningLine by mutableStateOf<List<Int>>(emptyList())
        private set

    fun playMove(index: Int) {
        if (board[index].isNotEmpty() || winner != null) return
        board[index] = currentPlayer
        evaluateBoard()
        if (winner == null) {
            currentPlayer = if (currentPlayer == "X") "O" else "X"
        }
    }

    fun reset() {
        for (i in board.indices) board[i] = ""
        winner = null
        winningLine = emptyList()
        currentPlayer = "X"
    }

    private fun evaluateBoard() {
        for (combination in WINNING_COMBINATIONS) {
            val (a, b, c) = combination
            if (board[a].isNotEmpty() && board[a] == board[b] && board[a] == board[c]) {
                winner = board[a]
                winningLine = combination
                return
            }
        }
        if ("" !in board) {
            winner = "draw"
        }
    }
}