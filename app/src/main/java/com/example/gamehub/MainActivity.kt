package com.example.gamehub

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.util.MutableInt
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gamehub.ui.theme.GameHubTheme
import androidx.navigation.compose.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DensityMedium
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ExposurePlus1
import androidx.compose.material.icons.filled.ExposureNeg1
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.center
import androidx.compose.ui.window.Dialog
import androidx.datastore.preferences.core.edit
import com.example.gamehub.settings.BoardGamePlayer
import com.example.gamehub.settings.CheckoutTable
import com.example.gamehub.settings.DartThrow
import com.example.gamehub.settings.Preference
import com.example.gamehub.settings.Settings
import com.example.gamehub.settings.ThemeMode
import com.example.gamehub.settings.dataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.text.isEmpty

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            var themeMode by remember { mutableStateOf(ThemeMode.LIGHT) }

            LaunchedEffect(Unit) {
                themeMode = Settings(context).getThemeMode()
            }

            val dartsPlayersNames = remember { mutableStateListOf<String>() }

            LaunchedEffect(Unit) {
                context.dataStore.data
                    .map { prefs ->
                        prefs[Preference.DARTS_PLAYER_NAME_LIST.key]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                    }
                    .collect { list ->
                        dartsPlayersNames.clear()
                        dartsPlayersNames.addAll(list)
                    }
            }

            var dartsGame by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                    dartsGame = Settings(context).get(Preference.DARTS_ACTIVE_GAME)
            }

            var dartsDoubleIn by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                dartsDoubleIn = Settings(context).get(Preference.DARTS_DOUBLE_IN)
            }

            var dartsDoubleOut by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                dartsDoubleOut = Settings(context).get(Preference.DARTS_DOUBLE_OUT)
            }

            val dartsGameMoves = remember { mutableStateListOf<DartThrow>() }

            LaunchedEffect(Unit) {
                val loaded = Settings(context).loadDartsGame()

                dartsGameMoves.clear()
                dartsGameMoves.addAll(loaded)
            }

            var dartsCurrentPlayer by remember { mutableIntStateOf(0) }

            LaunchedEffect(Unit) {
                dartsCurrentPlayer = Settings(context).get(Preference.DARTS_CURRENT_PLAYER)
            }

            val inputStream = resources.openRawResource(R.raw.checkout_table)
            val json = inputStream.bufferedReader().use { it.readText() }
            val checkoutTable = Json.decodeFromString<List<CheckoutTable>>(json)

            val boardGamePlayers = remember { mutableStateListOf<BoardGamePlayer>() }

            LaunchedEffect(Unit) {
                val loaded = Settings(context).loadBoardGamePlayers()
                boardGamePlayers.clear()
                boardGamePlayers.addAll(loaded)
            }

            var boardGameWinScore by remember { mutableIntStateOf(0) }

            LaunchedEffect(Unit) {
                boardGameWinScore = Settings(context).get(Preference.BOARD_GAME_WIN_SCORE)
            }

            GameHubTheme(themeMode) {
                val navController = rememberNavController()
                val buttonColors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background,
                    disabledContainerColor = MaterialTheme.colorScheme.secondary,
                    disabledContentColor = MaterialTheme.colorScheme.tertiary
                )
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {

                    composable("home") {
                        HomeScreen(navController, buttonColors)
                    }

                    composable("settings"){
                        SettingsScreen(
                            navController,
                            buttonColors,
                            themeMode = themeMode,
                            onThemeChange = { newTheme ->
                                themeMode = newTheme

                                scope.launch {
                                    Settings(context).setThemeMode(newTheme)
                                }
                            })
                    }

                    composable("tictactoe") {
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Krizic Kruzic",
                            onArrowClick = "home",
                            content = { TicTacToeGrid(navController, buttonColors) }
                        )
                    }

                    composable("tictactoebot") {
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Krizic Kruzic Bot",
                            onArrowClick = "home",
                            content = { TicTacToeBotGrid(navController, buttonColors) }
                        )
                    }

                    composable("dartsMenu") {
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Pikado",
                            onArrowClick = "home",
                            content = { DartsMenu(navController, buttonColors) }
                        )
                    }

                    composable("dartsNewGame") {
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Pikado",
                            onArrowClick = "dartsMenu",
                            content = { DartsNewGame(navController, buttonColors,
                                onButtonPress = { newDartsPlayerNames: List<String>, newDartsGame: String ->
                                    dartsPlayersNames.clear()
                                    dartsPlayersNames.addAll(newDartsPlayerNames)
                                    dartsGame = newDartsGame
                                    scope.launch {
                                        context.dataStore.edit { prefs ->
                                            prefs[Preference.DARTS_PLAYER_NAME_LIST.key] = dartsPlayersNames.joinToString(",")
                                        }
                                        context.dataStore.edit { prefs ->
                                            prefs[Preference.DARTS_ACTIVE_GAME.key] = newDartsGame
                                        }
                                    }
                                },
                                onDoubleInPressed = { newDoubleIn: Boolean ->
                                    dartsDoubleIn = newDoubleIn
                                    scope.launch {
                                        context.dataStore.edit { prefs ->
                                            prefs[Preference.DARTS_DOUBLE_IN.key] = newDoubleIn
                                        }
                                    }
                                },
                                onDoubleOutPressed = { newDoubleOut: Boolean ->
                                    dartsDoubleOut = newDoubleOut
                                    scope.launch {
                                        context.dataStore.edit { prefs ->
                                            prefs[Preference.DARTS_DOUBLE_OUT.key] = newDoubleOut
                                        }
                                    }
                                },
                                saveCurrPlayer = { currPlayer: Int ->
                                    dartsCurrentPlayer = currPlayer % dartsPlayersNames.count()
                                    scope.launch {
                                        context.dataStore.edit { prefs ->
                                            prefs[Preference.DARTS_CURRENT_PLAYER.key] = currPlayer % dartsPlayersNames.count()
                                        }
                                    }
                                })
                            }
                        )
                    }

                    composable("darts") {
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Pikado: $dartsGame",
                            onArrowClick = "dartsMenu",
                            content = { Darts(
                                navController,
                                buttonColors,
                                dartsPlayersNames,
                                dartsGame,
                                dartsDoubleIn,
                                dartsDoubleOut,
                                true,
                                dartsGameMoves,
                                dartsCurrentPlayer,
                                checkoutTable,
                                saveMoves = { moves ->
                                    //dartsGameMoves.clear()
                                    //dartsGameMoves.addAll(moves)
                                    scope.launch {
                                        Settings(context).saveDartsGame(moves)
                                    }
                                },
                                saveCurrPlayer = { currPlayer: Int ->
                                    dartsCurrentPlayer = currPlayer % dartsPlayersNames.count()
                                    scope.launch {
                                        context.dataStore.edit { prefs ->
                                            prefs[Preference.DARTS_CURRENT_PLAYER.key] = currPlayer % dartsPlayersNames.count()
                                        }
                                    }
                                }
                            ) }
                        )
                    }
                    composable("dartsNew") {
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Pikado: $dartsGame",
                            onArrowClick = "dartsMenu",
                            content = { Darts(navController, buttonColors, dartsPlayersNames, dartsGame, dartsDoubleIn, dartsDoubleOut, false, dartsGameMoves, dartsCurrentPlayer, checkoutTable,
                                saveMoves = { moves ->
                                    scope.launch {
                                        Settings(context).saveDartsGame(moves)
                                    }
                                },
                                saveCurrPlayer = { currPlayer: Int ->
                                    dartsCurrentPlayer = currPlayer % dartsPlayersNames.count()
                                    scope.launch {
                                        context.dataStore.edit { prefs ->
                                            prefs[Preference.DARTS_CURRENT_PLAYER.key] = currPlayer % dartsPlayersNames.count()
                                        }
                                    }
                                }
                            ) }
                        )
                    }
                    composable("dartsGameDisplay"){
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Pikado Potezi",
                            onArrowClick = "darts",
                            content = { DartsMovesList(navController, buttonColors, dartsPlayersNames, dartsGameMoves, dartsCurrentPlayer,
                                saveMoves = { moves ->
                                    dartsGameMoves.clear()
                                    dartsGameMoves.addAll(moves)
                                    scope.launch {
                                        Settings(context).saveDartsGame(moves)
                                    }
                                },
                                saveCurrPlayer = { currPlayer: Int ->
                                    dartsCurrentPlayer = currPlayer % dartsPlayersNames.count()
                                    scope.launch {
                                        context.dataStore.edit { prefs ->
                                            prefs[Preference.DARTS_CURRENT_PLAYER.key] = currPlayer % dartsPlayersNames.count()
                                        }
                                    }
                                }
                            ) }
                        )
                    }
                    composable("boardGames"){
                        GameScaffold(
                            navController = navController,
                            buttonColors = buttonColors,
                            title = "Igre na Ploči",
                            onArrowClick = "home",
                            content = { BoardGames(navController, buttonColors, boardGamePlayers, boardGameWinScore,
                                savePlayers = { players: List<BoardGamePlayer> ->
                                    boardGamePlayers.clear()
                                    boardGamePlayers.addAll(players)
                                    scope.launch {
                                        val res = Settings(context).saveBoardGamePlayers(players)
                                    }
                                },
                                saveWinScore = { score: Int ->
                                    boardGameWinScore = score
                                    scope.launch {
                                        context.dataStore.edit { prefs ->
                                            prefs[Preference.BOARD_GAME_WIN_SCORE.key] = score
                                        }
                                    }
                                })
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
        /**
         * Prikazuje počeni ekran za biranje igre
         */
fun HomeScreen(navController: NavController, buttonColors: ButtonColors) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Button(
            onClick = { navController.navigate("tictactoe") },
            modifier = Modifier
                .padding(16.dp)
                .width(screenWidth / 1.1f)
                .height(screenWidth / 4),
            colors = buttonColors
        ) {
            Text(text = "Krizic Kruzic",
                fontSize = (screenWidth.value * 0.08f).sp)
        }

        Button(
            onClick = { navController.navigate("dartsMenu") },
            modifier = Modifier
                .padding(16.dp)
                .width(screenWidth / 1.1f)
                .height(screenWidth / 4),
            colors = buttonColors
        ) {
            Text(text = "Pikado",
                fontSize = (screenWidth.value * 0.08f).sp)
        }

        Button(
            onClick = { navController.navigate("boardGames") },
            modifier = Modifier
                .padding(16.dp)
                .width(screenWidth / 1.1f)
                .height(screenWidth / 4),
            colors = buttonColors
        ) {
            Text(text = "Društvene Igre",
                fontSize = (screenWidth.value * 0.08f).sp)
        }

        //Button(
        //    onClick = { navController.navigate("snake") },
        //    modifier = Modifier.padding(16.dp)
        //) {
        //    Text("Snake")
        //}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * Prikazuje ekran za postavke
         * @param themeMode Trenutna postavljena tema
         * @param onThemeChange Lambda funkcija za spremanje promjene teme
         */
fun SettingsScreen(navController: NavController,
                   buttonColors: ButtonColors,
                   themeMode: ThemeMode,
                   onThemeChange: (ThemeMode) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Postavke",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            textAlign = TextAlign.Center,
                            fontSize = (screenWidth.value * 0.08f).sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Nazad"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("home") },
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Text(text = "Tema: ${themeMode.name}",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = (screenWidth.value * 0.08f).sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = themeMode == ThemeMode.DARK,
                        onCheckedChange = { isDark ->
                            onThemeChange(
                                if (isDark) ThemeMode.DARK else ThemeMode.LIGHT
                            )
                        },
                        modifier = Modifier.size(screenWidth / 4)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * Prikazuje gornju navigacijsku traku
         * @param title Ime igre
         * @param onArrowClick String za navController u slučaju pritiska strelice
         * @param content Lambda funkcija koja poziva funkciju igre
         */
fun GameScaffold(
    navController: NavController,
    buttonColors: ButtonColors,
    title: String,
    onArrowClick: String,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = title, fontSize = (screenWidth.value * 0.06f).sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(onArrowClick) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Nazad"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Postavke"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content()
        }
    }
}

@Composable
        /**
         * Prikazuje grid za igranje križić kružić igre protiv igrača
         */
fun TicTacToeGrid(navController: NavController, buttonColors: ButtonColors) {
    val board = remember {
        mutableStateListOf("", "", "", "", "", "", "", "", "")
    }

    var currentPlayer by remember { mutableStateOf("X") }
    var winner by remember { mutableStateOf<String?>(null) }
    var winningLine by remember { mutableStateOf<List<Int>>(emptyList()) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val winningCombinations = listOf(
        listOf(0,1,2),
        listOf(3,4,5),
        listOf(6,7,8),
        listOf(0,3,6),
        listOf(1,4,7),
        listOf(2,5,8),
        listOf(0,4,8),
        listOf(2,4,6)
    )

    fun checkWinner() {
        for (combination in winningCombinations) {
            val (a,b,c) = combination
            if (board[a].isNotEmpty() &&
                board[a] == board[b] &&
                board[a] == board[c]
            ) {
                winner = board[a]
                winningLine = combination
                return
            }
        }
        if ("" !in board){
            winner = "draw"
            return
        }
    }
    Text(
        text = "Na potezu: $currentPlayer",
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
        textAlign = TextAlign.Center,
        fontSize = (screenWidth.value * 0.08f).sp,
        color = MaterialTheme.colorScheme.tertiary
    )
    IconButton(
        onClick = { navController.navigate("tictactoebot") },
        modifier = Modifier
            .padding(10.dp)
            .width(screenWidth / 5)
    ) {
        Icon(
            imageVector = Icons.Default.Adb,
            contentDescription = "Bot",
            modifier = Modifier.size((screenWidth.value * 0.08f).dp)
        )
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()

    ) {
        for (row in 0..2) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ){
                for (col in 0..2) {

                    val index = row * 3 + col
                    val isWinningCell = index in winningLine

                    Button(
                        onClick = {
                            if (board[index].isEmpty() && winner == null) {
                                board[index] = currentPlayer
                                checkWinner()
                                if (winner == null) {
                                    currentPlayer =
                                        if (currentPlayer == "X") "O" else "X"
                                }
                            }
                        },
                        enabled = board[index].isEmpty() && winner == null,
                        modifier = Modifier
                            .padding(8.dp)
                            .width(screenWidth / 4)
                            .height(screenWidth / 4),
                        colors = buttonColors
                    ) {
                        Text(
                            text = board[index],
                            color = if (isWinningCell)
                                Color.Red
                            else
                                Color.Unspecified,
                            fontSize = (screenWidth.value * 0.1f).sp
                        )
                    }
                }
            }
        }
    }
    if (winner != null) {

        Dialog(
            onDismissRequest = { /* sprječavamo zatvaranje klikom van */ }
        ) {

            AnimatedVisibility(
                visible = winner != null,
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
                        modifier = Modifier
                            .padding(24.dp)
                    ) {

                        Text(
                            text = if (winner == "draw")
                                "Neriješeno"
                            else
                                "Pobjednik: $winner",
                            fontSize = (screenWidth.value * 0.08f).sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                for (i in board.indices) board[i] = ""
                                winner = null
                                winningLine = emptyList()
                                currentPlayer = "X"
                            },
                            modifier = Modifier
                                .width(screenWidth / 2)
                                .height(screenWidth / 6),
                            colors = buttonColors
                        ) {
                            Text(
                                text = "Restart",
                                fontSize = (screenWidth.value * 0.06f).sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
        /**
         * Prikazuje grid za igranje križić kružić igre protiv bota
         */
fun TicTacToeBotGrid(navController: NavController, buttonColors: ButtonColors) {
    val board = remember {
        mutableStateListOf("", "", "", "", "", "", "", "", "")
    }

    var winner by remember { mutableStateOf<String?>(null) }
    var winningLine by remember { mutableStateOf<List<Int>>(emptyList()) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val winningCombinations = listOf(
        listOf(0,1,2),
        listOf(3,4,5),
        listOf(6,7,8),
        listOf(0,3,6),
        listOf(1,4,7),
        listOf(2,5,8),
        listOf(0,4,8),
        listOf(2,4,6)
    )

    var startingPlayer by remember { mutableIntStateOf(if (Math.random() > 0.5) 0 else 1) }
    val botSymbol = if (startingPlayer == 0) "X" else "O"
    val playerSymbol = if (startingPlayer == 0) "O" else "X"
    var currentPlayer by remember {
        mutableStateOf(if (startingPlayer == 0) "Bot" else "Vi")
    }

    fun checkWinner() {
        for (combination in winningCombinations) {
            val (a,b,c) = combination
            if (board[a].isNotEmpty() &&
                board[a] == board[b] &&
                board[a] == board[c]
            ) {
                winner = board[a]
                winningLine = combination
                return
            }
        }
        if ("" !in board){
            winner = "draw"
            return
        }
    }

    fun checkWin(board: List<String>): String {
        for (combination in winningCombinations) {
            val (a,b,c) = combination
            if (board[a].isNotEmpty() &&
                board[a] == board[b] &&
                board[a] == board[c]
            ) {
                return board[a]
            }
        }
        if ("" !in board) {
            return "draw"
        }
        return ""
    }

    fun canIWin(): Int {
        val tempBoard = board.toMutableList()
        for (i in 0..8){
            if (tempBoard[i].isEmpty()){
                tempBoard[i] = botSymbol
                if (checkWin(tempBoard) == botSymbol) return i
                tempBoard[i] = ""
            }
        }
        return -1
    }

    fun minMax(board: MutableList<String>, isMaximizing: Boolean): Int {

        val result = checkWin(board)

        if (result == botSymbol) return 1
        if (result == playerSymbol) return -1
        if (result == "draw") return 0

        if (isMaximizing) {
            var bestScore = Int.MIN_VALUE

            for (i in board.indices) {
                if (board[i].isEmpty()) {
                    board[i] = botSymbol
                    val score = minMax(board, false)
                    board[i] = ""
                    bestScore = maxOf(score, bestScore)
                }
            }
            return bestScore
        } else {
            var bestScore = Int.MAX_VALUE

            for (i in board.indices) {
                if (board[i].isEmpty()) {
                    board[i] = playerSymbol
                    val score = minMax(board, true)
                    board[i] = ""
                    bestScore = minOf(score, bestScore)
                }
            }
            return bestScore
        }
    }

    fun findBestMove(): Int {
        val pobjedaIndex = canIWin()
        if (pobjedaIndex != -1) return pobjedaIndex

        var bestScore = Int.MIN_VALUE
        var move = -1

        val tempBoard = board.toMutableList()

        for (i in tempBoard.indices) {
            if (tempBoard[i].isEmpty()) {

                tempBoard[i] = botSymbol
                val score = minMax(tempBoard, false)
                tempBoard[i] = ""

                if (score > bestScore) {
                    bestScore = score
                    move = i
                }
            }
        }
        return move
    }

    fun botMove() {
        if (board[4].isEmpty()) {
            board[4] = botSymbol
        } else {
            val najPotez = findBestMove()
            board[najPotez] = botSymbol
        }

        checkWinner()

        if (winner == null) {
            currentPlayer = "Vi"
        }
    }

    Text(
        text = "Na potezu: $currentPlayer",
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
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ){
                for (col in 0..2) {

                    val index = row * 3 + col
                    val isWinningCell = index in winningLine

                    Button(
                        onClick = {
                            if (board[index].isEmpty() && winner == null) {
                                board[index] = playerSymbol
                                checkWinner()
                                if (winner == null) {
                                    currentPlayer = "Bot"
                                }
                            }
                        },
                        enabled = board[index].isEmpty() && winner == null,
                        modifier = Modifier
                            .padding(8.dp)
                            .width(screenWidth / 4)
                            .height(screenWidth / 4),
                        colors = buttonColors
                    ) {
                        Text(
                            text = board[index],
                            color = if (isWinningCell)
                                Color.Red
                            else
                                Color.Unspecified,
                            fontSize = (screenWidth.value * 0.1f).sp
                        )
                    }
                }
            }
        }
    }
    LaunchedEffect(currentPlayer, winner) {
        if (currentPlayer == "Bot" && winner == null && (playerSymbol in board || botSymbol in board)) {
            delay(300)
            botMove()
        }
    }
    LaunchedEffect(Unit, winner) {
        if (currentPlayer == "Bot" && winner == null && botSymbol !in board) {
            delay(300)
            botMove()
        }
    }
    if (winner != null) {

        Dialog(
            onDismissRequest = { /* sprječavamo zatvaranje klikom van */ }
        ) {

            AnimatedVisibility(
                visible = winner != null,
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
                        modifier = Modifier
                            .padding(24.dp)
                    ) {

                        Text(
                            text = if (winner == "draw")
                                "Neriješeno"
                            else
                                "Pobjednik: $winner",
                            fontSize = (screenWidth.value * 0.08f).sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                for (i in board.indices) board[i] = ""
                                winner = null
                                winningLine = emptyList()
                                startingPlayer = if (Math.random() > 0.5) 0 else 1
                                currentPlayer = if (startingPlayer == 0) "Bot" else "Vi"
                            },
                            modifier = Modifier
                                .width(screenWidth / 2)
                                .height(screenWidth / 6),
                            colors = buttonColors
                        ) {
                            Text(
                                text = "Restart",
                                fontSize = (screenWidth.value * 0.06f).sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
        /**
         * Funkcija za crtanje ploče za pikado
         */
fun DartsBoard(navController: NavController, buttonColors: ButtonColors, modifier: Modifier) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight()
    ) {
        Canvas(modifier = modifier) {
            val center = Offset(size.width / 2, size.height / 2)

            val numbers = listOf(
                20, 1, 18, 4, 13, 6, 10, 15, 2, 17,
                3, 19, 7, 16, 8, 11, 14, 9, 12, 5
            )

            val segmentAngle = 360f / 20f

            val radius = size.minDimension / 2

            val doubleOuter = radius * 0.85f
            val doubleInner = doubleOuter * (0.85f / 0.95f)
            val tripleOuter = doubleOuter * (0.60f / 0.95f)
            val tripleInner = doubleOuter * (0.52f / 0.95f)
            val outerBull = doubleOuter * (0.15f / 0.95f)
            val innerBull = doubleOuter * (0.05f / 0.95f)

            // Crna podloga
            drawCircle(Color.Black, radius, center)

            // PROLAZ 1: Double ring (vanjski sloj)
            for (i in 0 until 20) {
                val startAngle = i * segmentAngle - 90f - segmentAngle / 2f
                val ringColor = if (i % 2 == 0) Color(0xFFD32F2F) else Color(0xFF2E7D32)

                drawArc(
                    color = ringColor,
                    startAngle = startAngle,
                    sweepAngle = segmentAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - doubleOuter, center.y - doubleOuter),
                    size = Size(doubleOuter * 2, doubleOuter * 2)
                )
            }

            // PROLAZ 2: Single vanjski (doubleInner) - prekriva unutrašnjost double
            for (i in 0 until 20) {
                val startAngle = i * segmentAngle - 90f - segmentAngle / 2f
                val mainColor = if (i % 2 == 0) Color(0xFF111111) else Color(0xFFF5F5F5)

                drawArc(
                    color = mainColor,
                    startAngle = startAngle,
                    sweepAngle = segmentAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - doubleInner, center.y - doubleInner),
                    size = Size(doubleInner * 2, doubleInner * 2)
                )
            }

            // PROLAZ 3: Triple ring
            for (i in 0 until 20) {
                val startAngle = i * segmentAngle - 90f - segmentAngle / 2f
                val ringColor = if (i % 2 == 0) Color(0xFFD32F2F) else Color(0xFF2E7D32)

                drawArc(
                    color = ringColor,
                    startAngle = startAngle,
                    sweepAngle = segmentAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - tripleOuter, center.y - tripleOuter),
                    size = Size(tripleOuter * 2, tripleOuter * 2)
                )
            }

            // PROLAZ 4: Single unutarnji (tripleInner) - prekriva unutrašnjost triple
            for (i in 0 until 20) {
                val startAngle = i * segmentAngle - 90f - segmentAngle / 2f
                val mainColor = if (i % 2 == 0) Color(0xFF111111) else Color(0xFFF5F5F5)

                drawArc(
                    color = mainColor,
                    startAngle = startAngle,
                    sweepAngle = segmentAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - tripleInner, center.y - tripleInner),
                    size = Size(tripleInner * 2, tripleInner * 2)
                )
            }

            // Metalni razdjelnici
            for (i in 0 until 20) {
                val startAngle = i * segmentAngle - 90f - segmentAngle / 2f
                val angleRad = Math.toRadians(startAngle.toDouble())
                val lineEnd = Offset(
                    center.x + doubleOuter * cos(angleRad).toFloat(),
                    center.y + doubleOuter * sin(angleRad).toFloat()
                )
                drawLine(
                    Color.Gray,
                    center,
                    lineEnd,
                    strokeWidth = 2f
                )
            }

            // Bullseye
            drawCircle(Color(0xFF2E7D32), outerBull, center)
            drawCircle(Color(0xFFD32F2F), innerBull, center)

            // Brojevi
            for (i in 0 until 20) {
                val startAngle = i * segmentAngle - 90f - segmentAngle / 2f

                drawContext.canvas.nativeCanvas.apply {
                    val textRadius = doubleOuter * 1.1f
                    val textAngle = Math.toRadians((startAngle + segmentAngle / 2).toDouble())

                    val x = center.x + textRadius * cos(textAngle).toFloat()
                    val y = center.y + textRadius * sin(textAngle).toFloat()

                    val paint = Paint().apply {
                        color = Color(0xFFFFFFFF).toArgb()
                        textSize = radius * 0.09f
                        textAlign = Paint.Align.CENTER
                        isFakeBoldText = true
                    }

                    val textHeight = paint.descent() - paint.ascent()
                    val textOffset = textHeight / 2 - paint.descent()

                    drawText(numbers[i].toString(), x, y + textOffset, paint)
                }
            }
        }
    }
}

@Composable
        /**
         * Prikazuje menu za odabir nove ili postojeće igre pikada
         */
fun DartsMenu(navController: NavController, buttonColors: ButtonColors) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ){
        Button(
            onClick = {navController.navigate("dartsNewGame")},
            modifier = Modifier
                .padding(8.dp)
                .width(screenWidth / 1.1f),
            colors = buttonColors
        ) {
            Text(
                text = "Nova Igra",
                fontSize = (screenWidth.value * 0.08f).sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        Button(
            onClick = {navController.navigate("darts")},
            modifier = Modifier
                .padding(8.dp)
                .width(screenWidth / 1.1f),
            colors = buttonColors
        ) {
            Text(
                text = "Nastavi Igru",
                fontSize = (screenWidth.value * 0.08f).sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
        /**
         * Prikazuje grid za unos parametara igre
         * @param onButtonPress Lambda funkcija za spremanje igre i igrača u memoriju
         * @param onDoubleInPressed Lambda funkcija za spremanje parametra DoubleIn u memoriju
         * @param onDoubleOutPressed Lambda funkcija za spremanje parametra DoubleOut u memoriju
         */
fun DartsNewGame(navController: NavController, buttonColors: ButtonColors, onButtonPress: (List<String>, String) -> Unit, onDoubleInPressed: (Boolean) -> Unit, onDoubleOutPressed: (Boolean) -> Unit, saveCurrPlayer: (Int) -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val playerNames = remember { mutableStateListOf("") }
    var newGame by remember { mutableStateOf("901") }
    var doubleIn by remember { mutableStateOf(false) }
    var doubleOut by remember { mutableStateOf(true) }
    var showError1 by remember { mutableStateOf(false) }
    var showError2 by remember { mutableStateOf(false) }
    var showError3 by remember { mutableStateOf(false) }
    val possibleGamesList = remember { mutableStateListOf("301", "501", "701", "901") } //TODO: dodaj cricket
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .pointerInput(Unit){
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                OutlinedTextField(
                    value = newGame,
                    onValueChange = { newText ->
                        newGame = newText
                    },
                    label = { Text("Igra:") },
                    singleLine = true,
                    modifier = Modifier
                        .width(screenWidth / 3f)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                if (newGame !in possibleGamesList) {
                                    newGame = "901"
                                    showError2 = true
                                } else {
                                    if (showError2) showError2 = false
                                }
                            }
                        }
                )
                Spacer(modifier = Modifier.width(screenWidth / 7))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ){
                        Text("Double-in ", fontSize = (screenWidth.value * 0.04f).sp)
                        Spacer(modifier = Modifier.width(screenWidth * 0.05f))
                        Switch(
                            checked = doubleIn,
                            onCheckedChange = {onDoubleInPressed(it)
                                              doubleIn = !doubleIn},
                            modifier = Modifier.size(screenWidth / 8)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ){
                        Text("Double-out", fontSize = (screenWidth.value * 0.04f).sp)
                        Spacer(modifier = Modifier.width(screenWidth * 0.05f))
                        Switch(
                            checked = doubleOut,
                            onCheckedChange = {onDoubleOutPressed(it)
                                              doubleOut = !doubleOut
                            },
                            modifier = Modifier.size(screenWidth / 8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "Unesite imena igrača",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                ElevatedButton(
                    colors = buttonColors,
                    onClick = {
                        playerNames.clear()
                        playerNames.add("")
                        newGame = "901"
                        doubleIn = false
                        doubleOut = true
                    }
                ) {
                    Text("Reset")
                }
            }

            AnimatedVisibility(
                visible = showError1,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "⚠️ Unesite barem jednog igrača!",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showError1 = false }) {
                            Text("OK", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showError2,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "⚠️ Igra mora biti jedna od ponuđenih: ${possibleGamesList.joinToString(", ")}",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showError2 = false }) {
                            Text("OK", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showError3,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "⚠️ Imena igrača mogu imati max 10 slova!",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showError1 = false }) {
                            Text("OK", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable container
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(playerNames.size, key = { index -> index }) { index ->
                    OutlinedTextField(
                        value = playerNames[index],
                        onValueChange = { newText ->
                            if (newText.length <= 10){
                                playerNames[index] = newText
                            } else {
                                showError3 = true
                            }
                            if (index == playerNames.lastIndex && newText.isNotBlank() && playerNames.count() < 8) {
                                playerNames.add("")
                            }

                            if (newText.isBlank() && index != playerNames.lastIndex) {
                                playerNames.removeAt(index)
                            }
                        },
                        label = { Text("Igrač ${index + 1}") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        ElevatedButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            colors = buttonColors,
            onClick = {
                if (playerNames.any { it.isNotBlank() }) {
                    onButtonPress(playerNames
                        .map {it.trim()}
                        .filter { it.isNotBlank() }, newGame)
                    onDoubleInPressed(doubleIn)
                    onDoubleOutPressed(doubleOut)
                    saveCurrPlayer(0)
                    navController.navigate("dartsNew")
                } else {
                    showError1 = true
                }
            }
        ) {
            Text("Pokreni igru")
        }
    }
    LaunchedEffect(showError1) {
        if (showError1) {
            delay(3000)
            showError1 = false
        }
    }
    LaunchedEffect(showError2) {
        if (showError2) {
            delay(10000)
            showError2 = false
        }
    }
    LaunchedEffect(showError3) {
        if (showError3) {
            delay(10000)
            showError3 = false
        }
    }
}

@Composable
        /**
         * Prikazuje listu svih odigranih poteza
         * @param dartsPlayersNames Lista imena igrača
         * @param dartsGameMoves Lista odigranih poteza
         * @param dartsCurrentPlayer Indeks igrača na potezu
         */
fun DartsMovesList(navController: NavController, buttonColors: ButtonColors, dartsPlayersNames: List<String>, dartsGameMoves: List<DartThrow>, dartsCurrentPlayer: Int, saveMoves: (List<DartThrow>) -> Unit, saveCurrPlayer: (Int) -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val gameMoves = remember { mutableStateListOf<DartThrow>() }
    var currentPlayer by remember { mutableIntStateOf(dartsCurrentPlayer) }

    LaunchedEffect(Unit) {
        if (gameMoves.isEmpty()){
            gameMoves.addAll(dartsGameMoves)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            Button(
                onClick = { navController.navigate("darts") },
                modifier = Modifier
                    .padding(10.dp)
            ) {
                Text(
                    text = "Odustani",
                    modifier = Modifier
                        .padding(15.dp),
                    textAlign = TextAlign.Center,
                    fontSize = (screenWidth.value * 0.05f).sp
                )
            }
            Button(
                onClick = {
                    Log.d("GAME_MOVES", "$gameMoves")
                    saveMoves(gameMoves)
                    saveCurrPlayer(currentPlayer)
                    navController.navigate("darts") },
                modifier = Modifier
                    .padding(10.dp)
            ) {
                Text(
                    text = "Spremi",
                    modifier = Modifier
                        .padding(15.dp),
                    textAlign = TextAlign.Center,
                    fontSize = (screenWidth.value * 0.05f).sp
                )
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(gameMoves.size, key = { index -> index }) { index ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(text = dartsPlayersNames[gameMoves[index].playerIndex],
                        modifier = Modifier.padding(10.dp),
                        fontSize = (screenWidth.value * 0.05f).sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ){
                        Text(text = gameMoves[index].throws[0].toString(),
                            modifier = Modifier.padding(10.dp),
                            fontSize = (screenWidth.value * 0.05f).sp)
                        Text(text = gameMoves[index].throws[1].toString(),
                            modifier = Modifier.padding(10.dp),
                            fontSize = (screenWidth.value * 0.05f).sp)
                        Text(text = gameMoves[index].throws[2].toString(),
                            modifier = Modifier.padding(10.dp),
                            fontSize = (screenWidth.value * 0.05f).sp)
                    }
                    Button(
                        modifier = Modifier.padding(10.dp).alpha(if (index != gameMoves.size - 1) 0.3f else 1f),
                        onClick = {
                            if (index == gameMoves.size - 1){
                                gameMoves.removeAt(index)
                                currentPlayer -= 1
                                if (currentPlayer == -1){
                                    currentPlayer = dartsPlayersNames.size - 1
                                }
                            }
                        }
                    ){
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "X",
                            modifier = Modifier.size((screenWidth.value * 0.08f).dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
        /**
         * Prikazuje grid za igranje pikada
         * @param playerList Lista igrača
         * @param dartsGame Igra koja se igra
         * @param doubleIn Je li aktivan Double In
         * @param doubleOut Je li aktivan Double Out
         * @param loadGame Boolean varijabla koja govori treba li poteze učitati iz memorije
         * @param gameMoves Lista poteza
         * @param currentPlayer Indeks u listi igrača koji je na potezu
         * @param saveMoves Lambda funkcija koja sprema listu poteza u memoriju
         * @param saveCurrPlayer Lambda funkcija za spremanje indeksa aktivnog igrača u memoriju
         */
fun Darts(
    navController: NavController,
    buttonColors: ButtonColors,
    playerList: List<String>,
    dartsGame: String,
    doubleIn: Boolean,
    doubleOut: Boolean,
    loadGame: Boolean,
    gameMoves: MutableList<DartThrow>,
    currentPlayer: Int,
    checkoutTable: List<CheckoutTable>,
    saveMoves: (List<DartThrow>) -> Unit,
    saveCurrPlayer: (Int) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    var strelica1 by remember { mutableIntStateOf(0) }
    var strelica1tekst by remember { mutableStateOf("") }
    var strelica2 by remember { mutableIntStateOf(0) }
    var strelica2tekst by remember { mutableStateOf("") }
    var strelica3 by remember { mutableIntStateOf(0) }
    var strelica3tekst by remember { mutableStateOf("") }
    var brojStrelica by remember { mutableIntStateOf(3) }
    var opacity1 by remember { mutableFloatStateOf(0.3f) }
    var opacity2 by remember { mutableFloatStateOf(0.3f) }
    var opacity3 by remember { mutableFloatStateOf(0.3f) }
    val scores = remember(playerList.size) {
        MutableList(playerList.size) { dartsGame.toInt() }.toMutableStateList()
    }
    val winners = remember { mutableStateListOf<String>() }

    if (currentPlayer == 0 && winners.isEmpty()){
        for (i in scores) {
            if (i == 0) {
                winners.add(playerList[scores.indexOf(i)])
            }
        }
    }

    if (!winners.isEmpty()){
        Dialog(
            onDismissRequest = { /* sprječavamo zatvaranje klikom van */ }
        ) {

            AnimatedVisibility(
                visible = !winners.isEmpty(),
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
                        modifier = Modifier
                            .padding(24.dp)
                    ) {

                        Text(
                            text = if (winners.count() > 1)
                                "Pobjednici: ${winners.joinToString(",") }}"
                            else
                                "Pobjednik: ${winners[0]}",
                            fontSize = (screenWidth.value * 0.08f).sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                val moves: List<DartThrow> = listOf()
                                saveMoves(moves)
                                navController.navigate("dartsNew")
                            },
                            modifier = Modifier
                                .width(screenWidth / 2)
                                .height(screenWidth / 6),
                            colors = buttonColors
                        ) {
                            Text(
                                text = "Restart",
                                fontSize = (screenWidth.value * 0.06f).sp
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(loadGame) {
        if (loadGame) {
            scores.indices.forEach { scores[it] = dartsGame.toInt() }

            for (game in gameMoves) {
                for (dart in game.throws) {
                    scores[game.playerIndex] -= dart
                }
            }
        }
    }
    LaunchedEffect(!loadGame) {
        if (!loadGame){
            gameMoves.clear()
            saveMoves(gameMoves)
        }
    }

    LaunchedEffect(brojStrelica) {
        val sum = scores[currentPlayer] - strelica1 - strelica2 - strelica3
        for (item in checkoutTable) {
            if (item.score == sum){
                if (strelica1 == 0) {
                    strelica1tekst = item.hand[0]
                    strelica2tekst = item.hand[1]
                    strelica3tekst = item.hand[2]
                } else if (strelica2 == 0) {
                    strelica2tekst = item.hand[0]
                    strelica3tekst = item.hand[1]
                } else if (strelica3 == 0) {
                    strelica3tekst = item.hand[0]
                }
            }
        }
    }

    val numbers = listOf(
        20, 1, 18, 4, 13, 6, 10, 15, 2, 17,
        3, 19, 7, 16, 8, 11, 14, 9, 12, 5
    )

    val radius = if (screenWidth < screenHeight) screenWidth / 2 else screenHeight / 2

    val doubleOuter = radius * 0.85f
    val doubleInner = doubleOuter * (0.85f / 0.95f)
    val tripleOuter = doubleOuter * (0.60f / 0.95f)
    val tripleInner = doubleOuter * (0.52f / 0.95f)
    val outerBull = doubleOuter * (0.15f / 0.95f)
    val innerBull = doubleOuter * (0.05f / 0.95f)

    data class PolarCoord(
        val radius: Float,
        val angle: Float // 0–360°
    )

    fun calculatePolarCoordinates(
        tap: Offset,
        center: IntOffset
    ): PolarCoord {

        val dx = tap.x - center.x
        val dy = center.y - tap.y

        val radius = sqrt(dx * dx + dy * dy)

        var angle = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()

        if (angle < 0) angle += 360f

        return PolarCoord(radius, angle)
    }

    fun getSegment(angle: Float): Int {
        val segmentAngle = 360f / 20f

        val adjustedAngle = (-(angle) + 90f + segmentAngle / 2f + 360f) % 360f

        val index = (adjustedAngle / segmentAngle).toInt().coerceIn(0, 19)

        return numbers[index]
    }

    fun addMove(playerIndex: Int, dart1: Int, dart2: Int, dart3: Int) {
        val move = DartThrow(playerIndex, listOf(dart1, dart2, dart3))
        gameMoves.add(move)
        saveMoves(gameMoves)
    }

    @Composable
    fun ScoreCard(
        name: String,
        score: Int,
        isActive: Boolean
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isActive) 2.dp else 1.dp,
                    color = if (isActive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {

            // Floating-style label
            Text(
                text = name.take(10),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = (-10).dp)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 2.dp)
            )

            // Score
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit){
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopStart),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.width(screenWidth / 3),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Image(
                    painter = painterResource(id = R.drawable.strelica_pikado),
                    contentDescription = "Strelica",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(10.dp)
                        .alpha(if (brojStrelica == 3) 1f else 0.3f)
                        .clickable {
                            if (strelica1tekst != "" && brojStrelica == 2) {
                                strelica1tekst = ""
                                brojStrelica += 1
                            }
                        },
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = strelica1tekst,
                    modifier = Modifier.alpha(opacity1)
                )
            }
            Row(
                modifier = Modifier.width(screenWidth / 3),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Image(
                    painter = painterResource(id = R.drawable.strelica_pikado),
                    contentDescription = "Strelica",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(10.dp)
                        .alpha(if (brojStrelica >= 2) 1f else 0.3f)
                        .clickable {
                            if (strelica2tekst != "" && brojStrelica == 1) {
                                strelica2tekst = ""
                                brojStrelica += 1
                            }
                        },
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = strelica2tekst,
                    modifier = Modifier.alpha(opacity2)
                )
            }
            Row(
                modifier = Modifier.width(screenWidth / 3),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Image(
                    painter = painterResource(id = R.drawable.strelica_pikado),
                    contentDescription = "Strelica",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(10.dp)
                        .alpha(if (brojStrelica >= 1) 1f else 0.3f)
                        .clickable {
                            if (strelica3tekst != "" && brojStrelica == 0) {
                                strelica3tekst = ""
                                brojStrelica += 1
                            }
                        },
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = strelica3tekst,
                    modifier = Modifier.alpha(opacity3)
                )
            }
        }
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text(text =  if (doubleIn) "Double-In: Da" else "Double-In: Ne", modifier = Modifier.padding(10.dp))
            Text(text = if (doubleOut) "Double-Out: Da" else "Double-Out: Ne", modifier = Modifier.padding(10.dp))
        }
        Column(
            modifier = Modifier.align(Alignment.TopEnd),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { navController.navigate("dartsGameDisplay") },
                modifier = Modifier
                    .padding(10.dp)
                    .width(screenWidth / 5)
            ) {
                Icon(
                    imageVector = Icons.Default.DensityMedium,
                    contentDescription = "Lista Poteza",
                    modifier = Modifier.size((screenWidth.value * 0.08f).dp)
                )
            }
            ElevatedButton(
                modifier = Modifier.padding(16.dp),
                colors = buttonColors,
                onClick = {

                        if (scores[currentPlayer] == dartsGame.toInt() && doubleIn){
                            if ("2x" in strelica1tekst){
                                scores[currentPlayer] -= strelica1 + strelica2 + strelica3
                            }
                            else if ("2x" in strelica2tekst) {
                                strelica1 = 0
                                scores[currentPlayer] -= strelica2 + strelica3
                            }
                            else if ("2x" in strelica3tekst) {
                                strelica2 = 0
                                strelica1 = 0
                                scores[currentPlayer] -= strelica3
                            } else {
                                strelica3 = 0
                                strelica2 = 0
                                strelica1 = 0
                            }
                        } else {
                            val sum = scores[currentPlayer] - strelica1 - strelica2 - strelica3
                            if (sum > 0)
                                if (doubleOut) {
                                    if (sum != 1) {
                                        scores[currentPlayer] = sum
                                    }
                                } else {
                                    scores[currentPlayer] = sum
                                }
                            else if (sum == 0) {
                                if (doubleOut) {
                                    if ("2x" in strelica3tekst) {
                                        scores[currentPlayer] = 0
                                    } else if (strelica3 == 0) {
                                        if ("2x" in strelica2tekst) {
                                            scores[currentPlayer] = 0
                                        } else if (strelica2 == 0) {
                                            if ("2x" in strelica1tekst) {
                                                scores[currentPlayer] = 0
                                            } else {
                                                strelica1 = 0
                                            }
                                        } else {
                                            strelica2 = 0
                                            strelica1 = 0
                                        }
                                    } else {
                                        strelica3 = 0
                                        strelica2 = 0
                                        strelica1 = 0
                                    }
                                } else {
                                    scores[currentPlayer] = 0
                                }
                            }
                        }
                        addMove(currentPlayer, strelica1, strelica2, strelica3)
                        saveCurrPlayer(currentPlayer + 1)
                        strelica1 = 0
                        strelica2 = 0
                        strelica3 = 0
                        strelica1tekst = ""
                        strelica2tekst = ""
                        strelica3tekst = ""
                        brojStrelica = 3
                }
            ) {
                Text(text = "Spremi",
                    fontSize = (screenWidth.value * 0.05f).sp)
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {

            items(playerList.count()) { index ->
                if (index < playerList.size) {
                    ScoreCard(
                        name = playerList[index],
                        score = scores.getOrNull(index) ?: 0,
                        isActive = index == currentPlayer
                    )
                } else {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2.2f)
                    )
                }
            }
        }
    }
    DartsBoard(navController, buttonColors, modifier = Modifier
        .size(screenWidth)
        .pointerInput(Unit) {
            val density = density  // LocalDensity
            val doubleOuterPx = with(density) { doubleOuter.toPx() }
            val doubleInnerPx = with(density) { doubleInner.toPx() }
            val tripleOuterPx = with(density) { tripleOuter.toPx() }
            val tripleInnerPx = with(density) { tripleInner.toPx() }
            val outerBullPx = with(density) { outerBull.toPx() }
            val innerBullPx = with(density) { innerBull.toPx() }
            detectTapGestures { tapOffset ->
                val polar = calculatePolarCoordinates(
                    tap = tapOffset,
                    center = size.center
                )
                if (polar.radius <= size.width / 2f) {
                    val segment = getSegment(polar.angle)
                    val multiplier = when {
                        polar.radius <= innerBullPx -> 50
                        polar.radius <= outerBullPx -> 25
                        polar.radius in tripleInnerPx..tripleOuterPx -> 3
                        polar.radius in doubleInnerPx..doubleOuterPx -> 2
                        polar.radius > doubleOuterPx -> 0
                        else -> 1
                    }
                    var finalScore = 0
                    var addOn = ""
                    when (multiplier) {
                        50 -> {
                            finalScore = 50
                            addOn = " (2xBull)"
                        }

                        25 -> {
                            finalScore = 25
                            addOn = " (Bull)"
                        }

                        0 -> {
                            finalScore = 0
                            addOn = " (Promašaj)"
                        }

                        2 -> {
                            finalScore = segment * multiplier
                            addOn = " (2x$segment)"
                        }

                        3 -> {
                            finalScore = segment * multiplier
                            addOn = " (3x$segment)"
                        }

                        1 -> {
                            finalScore = segment
                            addOn = ""
                        }
                    }
                    when (brojStrelica) {
                        3 -> {
                            strelica1 = finalScore
                            strelica1tekst = "$strelica1$addOn"
                            brojStrelica -= 1
                            opacity1 = 1f
                        }

                        2 -> {
                            strelica2 = finalScore
                            strelica2tekst = "$strelica2$addOn"
                            brojStrelica -= 1
                            opacity2 = 1f
                        }

                        1 -> {
                            strelica3 = finalScore
                            strelica3tekst = "$strelica3$addOn"
                            brojStrelica -= 1
                            opacity3 = 1f
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun BoardGames(navController: NavController, buttonColors: ButtonColors, boardGamePlayers: List<BoardGamePlayer>, boardGameWinScore: Int, savePlayers: (List<BoardGamePlayer>) -> Unit, saveWinScore: (Int) -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    var winScore by remember { mutableStateOf(boardGameWinScore.toString()) }
    val playerList = remember { mutableStateListOf<BoardGamePlayer>() }
    var showError1 by remember { mutableStateOf(false) }
    var popUp1 by remember { mutableStateOf(false) }
    var popUp2 by remember { mutableStateOf(false) }
    var popUp3 by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        if (playerList.isEmpty()) {
            winScore = boardGameWinScore.toString()
            playerList.addAll(boardGamePlayers)
            if (playerList.isEmpty()){
                val newPlayer = BoardGamePlayer("", 0, 0)
                playerList.add(newPlayer)
            }
        }
    }
    @Composable
    fun ScoreCard(
        score: Int,
        winNum: Int
    ) {
        Box(
            modifier = Modifier
                .width(screenWidth / 8)
                .padding(12.dp)
        ) {

            // Floating-style label
            Text(
                text = "Pobjede: $winNum",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = (-10).dp)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 2.dp)
            )

            // Score
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit){
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                OutlinedTextField(
                    value = winScore,
                    onValueChange = { newText: String ->
                        winScore = newText },
                    label = { Text("Za Pobjedu:") },
                    singleLine = true,
                    modifier = Modifier
                        .width(screenWidth / 3f)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                val winScoreInt = winScore.toIntOrNull()
                                if (winScoreInt == null) {
                                    winScore = boardGameWinScore.toString()
                                    showError1 = true
                                } else {
                                    saveWinScore(winScore.toInt())
                                }
                            }
                        }
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Button(
                        onClick = {
                            popUp1 = true
                        },
                        modifier = Modifier.width(screenWidth / 6).height(screenWidth / 6),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.kocka),
                            contentDescription = "Kocka",
                            modifier = Modifier
                                .scale(2.2f),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            popUp2 = true
                        },
                        shape = CircleShape,
                        modifier = Modifier.size(screenWidth / 6)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Coin",
                            modifier = Modifier.scale(2.2f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "Unesite imena igrača",
                    fontSize = (screenWidth.value * 0.05f).sp,
                    fontWeight = FontWeight.Bold
                )
                ElevatedButton(
                    colors = buttonColors,
                    onClick = {
                        popUp3 = true
                    }
                ) {
                    Text(text = "Reset",
                        fontSize = (screenWidth.value * 0.05f).sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(
                visible = showError1,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "⚠️ Unos u polje za pobjedu mora biti broj!",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showError1 = false }) {
                            Text("OK", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(playerList.size, key = { index -> index }) { index ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedTextField(
                            value = playerList[index].playerName,
                            onValueChange = { newText: String ->
                                playerList[index] = playerList[index].copy(playerName = newText)
                                if (index == playerList.lastIndex && newText.isNotBlank()) {
                                    val newBoardGamePlayer = BoardGamePlayer("", 0, 0)
                                    playerList.add(newBoardGamePlayer)
                                    savePlayers(playerList)
                                }

                                if (newText.isBlank() && index != boardGamePlayers.lastIndex) {
                                    playerList.removeAt(index)
                                    savePlayers(playerList)
                                }
                            },
                            label = { Text(if (playerList[index].playerName != "") "Pobjede: ${playerList[index].winNumber}" else "Novi igrač") },
                            singleLine = true,
                            modifier = Modifier.width(screenWidth / 3)
                        )
                        Text(text = playerList[index].score.toString(), fontSize = (screenWidth.value * 0.08f).sp)
                        Button(
                            colors = buttonColors,
                            modifier = Modifier.width(screenWidth / 8).height(screenWidth / 8)
                                .alpha(if (playerList[index].playerName == "") 0.3f else 1f),
                            shape = RoundedCornerShape(6.dp),
                            onClick = {
                                if (playerList[index].playerName != "") {
                                    playerList[index] =
                                        playerList[index].copy(score = playerList[index].score + 1)
                                    savePlayers(playerList)
                                    if (boardGameWinScore != 0) {
                                        if (playerList[index].score >= boardGameWinScore) {
                                            winner = playerList[index].playerName
                                            playerList[index].winNumber += 1
                                        }
                                    }
                                }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ){
                            Icon(
                                imageVector = Icons.Default.ExposurePlus1,
                                contentDescription = "plusOne",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Button(
                            colors = buttonColors,
                            modifier = Modifier.width(screenWidth / 8).height(screenWidth / 8)
                                .alpha(if (playerList[index].playerName == "") 0.3f else 1f),
                            shape = RoundedCornerShape(6.dp),
                            onClick = {
                                if (playerList[index].playerName != "") {
                                    playerList[index] =
                                        playerList[index].copy(score = playerList[index].score - 1)
                                    savePlayers(playerList)
                                    if (boardGameWinScore != 0) {
                                        if (playerList[index].score >= boardGameWinScore) winner =
                                            playerList[index].playerName
                                    }
                                }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ){
                            Icon(
                                imageVector = Icons.Default.ExposureNeg1,
                                contentDescription = "minusOne",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(showError1) {
        if (showError1) {
            delay(10000)
            showError1 = false
        }
    }
    if (winner != "") {
        Dialog(
            onDismissRequest = { /* sprječavamo zatvaranje klikom van */ }
        ) {

            AnimatedVisibility(
                visible = winner != "",
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
                        modifier = Modifier
                            .padding(24.dp)
                    ) {

                        Text(
                            text = "Pobjednik: $winner",
                            fontSize = (screenWidth.value * 0.06f).sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                playerList.forEachIndexed { index, player ->
                                    playerList[index] =
                                        playerList[index].copy(score = 0)
                                }
                                savePlayers(playerList)
                                winner = ""
                            },
                            modifier = Modifier
                                .width(screenWidth / 2)
                                .height(screenWidth / 6),
                            colors = buttonColors
                        ) {
                            Text(
                                text = "Nastavi",
                                fontSize = (screenWidth.value * 0.06f).sp
                            )
                        }
                    }
                }
            }
        }
    }
    if (popUp1) {
        Dialog(
            onDismissRequest = { popUp1 = false }
        ) {

            AnimatedVisibility(
                visible = popUp1,
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
                        modifier = Modifier
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "${(Math.random() * 6).toInt() + 1}",
                            fontSize = (screenWidth.value * 0.08f).sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                popUp1 = false
                            },
                            modifier = Modifier
                                .width(screenWidth / 2)
                                .height(screenWidth / 6),
                            colors = buttonColors
                        ) {
                            Text(
                                text = "OK",
                                fontSize = (screenWidth.value * 0.06f).sp
                            )
                        }
                    }
                }
            }
        }
    }
    if (popUp2) {
        Dialog(
            onDismissRequest = { popUp2 = false }
        ) {

            AnimatedVisibility(
                visible = popUp2,
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
                        modifier = Modifier
                            .padding(24.dp)
                    ) {

                        Text(
                            text = if (Math.random() > 0.5) "Glava" else "Pismo",
                            fontSize = (screenWidth.value * 0.08f).sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                popUp2 = false
                            },
                            modifier = Modifier
                                .width(screenWidth / 2)
                                .height(screenWidth / 6),
                            colors = buttonColors
                        ) {
                            Text(
                                text = "OK",
                                fontSize = (screenWidth.value * 0.06f).sp
                            )
                        }
                    }
                }
            }
        }
    }
    if (popUp3) {
        Dialog(
            onDismissRequest = { popUp3 = false }
        ) {

            AnimatedVisibility(
                visible = popUp3,
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
                        modifier = Modifier
                            .padding(24.dp)
                    ) {

                        Text(
                            text = "Jeste li sigurni da želite resetirati?",
                            fontSize = (screenWidth.value * 0.08f).sp,
                            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                lineHeight = (screenWidth.value * 0.08f).sp
                            )
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ){
                            Button(
                                onClick = {
                                    popUp3 = false
                                },
                                modifier = Modifier
                                    .width(screenWidth / 4)
                                    .height(screenWidth / 6),
                                colors = buttonColors
                            ) {
                                Text(
                                    text = "Ne",
                                    fontSize = (screenWidth.value * 0.06f).sp
                                )
                            }
                            Spacer(modifier = Modifier.width(screenWidth / 10))
                            Button(
                                onClick = {
                                    popUp3 = false
                                    for (player in playerList) {
                                        player.score = 0
                                        player.winNumber = 0
                                    }
                                    savePlayers(playerList)
                                    saveWinScore(0)
                                    navController.navigate("boardGames")
                                },
                                modifier = Modifier
                                    .width(screenWidth / 4)
                                    .height(screenWidth / 6),
                                colors = buttonColors
                            ) {
                                Text(
                                    text = "Da",
                                    fontSize = (screenWidth.value * 0.06f).sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}