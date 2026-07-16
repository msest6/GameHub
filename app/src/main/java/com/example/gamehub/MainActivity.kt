package com.example.gamehub

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gamehub.data.PreferencesDataStore
import com.example.gamehub.data.ThemeRepository
import com.example.gamehub.data.model.ThemeMode
import com.example.gamehub.tictactoe.TicTacToeBotGrid
import com.example.gamehub.tictactoe.TicTacToeBotViewModel
import com.example.gamehub.tictactoe.TicTacToeGrid
import com.example.gamehub.tictactoe.TicTacToeViewModel
import com.example.gamehub.ui.HomeScreen
import com.example.gamehub.ui.SettingsScreen
import com.example.gamehub.ui.common.GameScaffold
import com.example.gamehub.ui.theme.GameHubTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            // Repozitoriji - jedina mjesta koja znaju za DataStore
            val prefs = remember { PreferencesDataStore(context) }
            val themeRepository = remember { ThemeRepository(prefs) }
            var themeMode by remember { mutableStateOf(ThemeMode.LIGHT) }

            LaunchedEffect(Unit) {
                themeMode = themeRepository.getThemeMode()
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

                    composable("settings") {
                        SettingsScreen(
                            navController,
                            buttonColors,
                            themeMode = themeMode,
                            onThemeChange = { newTheme ->
                                themeMode = newTheme
                                scope.launch {
                                    themeRepository.setThemeMode(newTheme)
                                }
                            })
                    }

                    composable("tictactoe") {
                        val ticTacToeViewModel: TicTacToeViewModel = viewModel()
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Krizic Kruzic",
                            onArrowClick = "home",
                            content = { TicTacToeGrid(navController, buttonColors, ticTacToeViewModel) }
                        )
                    }

                    composable("tictactoebot") {
                        val ticTacToeBotViewModel: TicTacToeBotViewModel = viewModel()
                        GameScaffold(
                            navController,
                            buttonColors,
                            title = "Krizic Kruzic Bot",
                            onArrowClick = "home",
                            content = { TicTacToeBotGrid(navController, buttonColors, ticTacToeBotViewModel) }
                        )
                    }
                }
            }
        }
    }
}