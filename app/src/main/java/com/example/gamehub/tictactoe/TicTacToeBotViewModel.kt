package com.example.gamehub.tictactoe

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
 * Drži cijeli state i logiku za križić-kružić igru protiv bota, uključujući
 * minimax algoritam za odabir bot poteza. Composable (TicTacToeBotGrid)
 * samo prikazuje stanje i prosljeđuje dodire igrača.
 */
class TicTacToeBotViewModel : ViewModel() {

    val board = mutableStateListOf("", "", "", "", "", "", "", "", "")

    var winner by mutableStateOf<String?>(null)
        private set

    var winningLine by mutableStateOf<List<Int>>(emptyList())
        private set

    /** "Bot" ili "Vi" - tko je trenutno na potezu. */
    var currentTurn by mutableStateOf("")
        private set

    private var startingPlayer by mutableStateOf(if (Math.random() > 0.5) 0 else 1)

    private val botSymbol: String get() = if (startingPlayer == 0) "X" else "O"
    private val playerSymbol: String get() = if (startingPlayer == 0) "O" else "X"

    init {
        currentTurn = if (startingPlayer == 0) "Bot" else "Vi"
        maybeTriggerBotMove()
    }

    fun playMove(index: Int) {
        if (currentTurn != "Vi" || board[index].isNotEmpty() || winner != null) return
        board[index] = playerSymbol
        evaluateBoard()
        if (winner == null) {
            currentTurn = "Bot"
            maybeTriggerBotMove()
        }
    }

    fun reset() {
        for (i in board.indices) board[i] = ""
        winner = null
        winningLine = emptyList()
        startingPlayer = if (Math.random() > 0.5) 0 else 1
        currentTurn = if (startingPlayer == 0) "Bot" else "Vi"
        maybeTriggerBotMove()
    }

    private fun maybeTriggerBotMove() {
        if (currentTurn != "Bot" || winner != null) return
        viewModelScope.launch {
            delay(300)
            botMove()
        }
    }

    private fun botMove() {
        if (board[4].isEmpty()) {
            board[4] = botSymbol
        } else {
            val bestMove = findBestMove()
            if (bestMove != -1) board[bestMove] = botSymbol
        }
        evaluateBoard()
        if (winner == null) {
            currentTurn = "Vi"
        }
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

    /** Čista pomoćna funkcija za simulacije nad kopijom ploče - ne dira stvarni state. */
    private fun checkWin(board: List<String>): String {
        for (combination in WINNING_COMBINATIONS) {
            val (a, b, c) = combination
            if (board[a].isNotEmpty() && board[a] == board[b] && board[a] == board[c]) {
                return board[a]
            }
        }
        if ("" !in board) return "draw"
        return ""
    }

    private fun canIWin(): Int {
        val temp = board.toMutableList()
        for (i in 0..8) {
            if (temp[i].isEmpty()) {
                temp[i] = botSymbol
                if (checkWin(temp) == botSymbol) return i
                temp[i] = ""
            }
        }
        return -1
    }

    private fun minMax(board: MutableList<String>, isMaximizing: Boolean): Int {
        val result = checkWin(board)
        if (result == botSymbol) return 1
        if (result == playerSymbol) return -1
        if (result == "draw") return 0

        return if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for (i in board.indices) {
                if (board[i].isEmpty()) {
                    board[i] = botSymbol
                    bestScore = maxOf(bestScore, minMax(board, false))
                    board[i] = ""
                }
            }
            bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for (i in board.indices) {
                if (board[i].isEmpty()) {
                    board[i] = playerSymbol
                    bestScore = minOf(bestScore, minMax(board, true))
                    board[i] = ""
                }
            }
            bestScore
        }
    }

    private fun findBestMove(): Int {
        val winIndex = canIWin()
        if (winIndex != -1) return winIndex

        var bestScore = Int.MIN_VALUE
        var move = -1
        val temp = board.toMutableList()
        for (i in temp.indices) {
            if (temp[i].isEmpty()) {
                temp[i] = botSymbol
                val score = minMax(temp, false)
                temp[i] = ""
                if (score > bestScore) {
                    bestScore = score
                    move = i
                }
            }
        }
        return move
    }
}