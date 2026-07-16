package com.example.gamehub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Arrangement

/**
 * Prikazuje početni ekran za biranje igre
 */
@Composable
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
            onClick = { navController.navigate("belaMenu") },
            modifier = Modifier
                .padding(16.dp)
                .width(screenWidth / 1.1f)
                .height(screenWidth / 4),
            colors = buttonColors
        ) {
            Text(
                text = "Bela kalkulator",
                fontSize = (screenWidth.value * 0.08f).sp
            )
        }

        Button(
            onClick = { navController.navigate("unoMenu") },
            modifier = Modifier
                .padding(16.dp)
                .width(screenWidth / 1.1f)
                .height(screenWidth / 4),
            colors = buttonColors
        ) {
            Text(
                text = "Uno Kalkulator",
                fontSize = (screenWidth.value * 0.08f).sp
            )
        }

        Button(
            onClick = { navController.navigate("boardGames") },
            modifier = Modifier
                .padding(16.dp)
                .width(screenWidth / 1.1f)
                .height(screenWidth / 4),
            colors = buttonColors
        ) {
            Text(
                text = "Društvene Igre",
                fontSize = (screenWidth.value * 0.08f).sp
            )
        }

        Button(
            onClick = { navController.navigate("tictactoe") },
            modifier = Modifier
                .padding(16.dp)
                .width(screenWidth / 1.1f)
                .height(screenWidth / 4),
            colors = buttonColors
        ) {
            Text(
                text = "Krizic Kruzic",
                fontSize = (screenWidth.value * 0.08f).sp
            )
        }

        Button(
            onClick = { navController.navigate("dartsMenu") },
            modifier = Modifier
                .padding(16.dp)
                .width(screenWidth / 1.1f)
                .height(screenWidth / 4),
            colors = buttonColors
        ) {
            Text(
                text = "Pikado kalkulator",
                fontSize = (screenWidth.value * 0.08f).sp
            )
        }

        Button(
            onClick = { navController.navigate("gradDrzavaMenu") },
            modifier = Modifier
                .padding(16.dp)
                .width(screenWidth / 1.1f)
                .height(screenWidth / 4),
            colors = buttonColors
        ) {
            Text(
                text = "Država Grad",
                fontSize = (screenWidth.value * 0.08f).sp
            )
        }

        //Button(
        //    onClick = { navController.navigate("snake") },
        //    modifier = Modifier.padding(16.dp)
        //) {
        //    Text("Snake")
        //}
    }
}