package com.example.gamehub.settings

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Serializable
data class DartThrow(
    val playerIndex: Int,
    val throws: List<Int>
)
@Serializable
data class BoardGamePlayer(
    var playerName: String,
    var score: Int,
    var winNumber: Int
){

}

class Settings(val context: Context) {
    suspend fun <T> get(preference: Preference<T>): T{
        return context.dataStore.data.first()[preference.key] ?: preference.defaultValue
    }
    suspend fun <T> set(preference: Preference<T>, value: T) {
        context.dataStore.edit {
            settings -> settings[preference.key] = value
        }
    }
    suspend fun getThemeMode(): ThemeMode {
        val str = get(Preference.THEME)
        return ThemeMode.valueOf(str)
    }
    suspend fun setThemeMode(themeMode: ThemeMode) {
        set(Preference.THEME, themeMode.name)
    }
    suspend fun saveDartsGame(gameMoves: List<DartThrow>) {
        val jsonString = Json.encodeToString(gameMoves)
        set(Preference.DARTS_GAME_RECORD, jsonString)
    }
    suspend fun loadDartsGame(): List<DartThrow> {
        val jsonString = get(Preference.DARTS_GAME_RECORD)
        return if (jsonString.isEmpty()) {
            emptyList()
        } else {
            Json.decodeFromString(jsonString)
        }
    }
    suspend fun saveBoardGamePlayers(players: List<BoardGamePlayer>) {
        val jsonString = Json.encodeToString(players)
        set(Preference.BOARD_GAME_PLAYERS, jsonString)
    }
    suspend fun loadBoardGamePlayers(): List<BoardGamePlayer> {
        val jsonString = get(Preference.BOARD_GAME_PLAYERS)
        return if (jsonString.isEmpty()) {
            emptyList()
        } else {
            Json.decodeFromString(jsonString)
        }
    }
}

enum class ThemeMode{
    LIGHT, DARK
}

data class Preference<T>(
    val key: Preferences.Key<T>,
    val defaultValue: T
){
    companion object {
        val THEME = Preference(stringPreferencesKey("theme"), "LIGHT")
        val DARTS_PLAYER_NAME_LIST = Preference(stringPreferencesKey("dartsPlayerNameList"), "")
        val DARTS_ACTIVE_GAME = Preference(stringPreferencesKey("dartsActiveGame"), "")
        val DARTS_DOUBLE_IN = Preference(booleanPreferencesKey("dartsDoubleIn"), false)
        val DARTS_DOUBLE_OUT = Preference(booleanPreferencesKey("dartsDoubleOut"), true)
        val DARTS_GAME_RECORD = Preference(stringPreferencesKey("dartsGameRecord"), "")
        val DARTS_CURRENT_PLAYER = Preference(intPreferencesKey("dartsCurrentPlayer"), 0)
        val BOARD_GAME_PLAYERS = Preference(stringPreferencesKey("boardGamePlayers"), "")
        val BOARD_GAME_WIN_SCORE = Preference(intPreferencesKey("boardGameWinScore"), 0)
    }
}