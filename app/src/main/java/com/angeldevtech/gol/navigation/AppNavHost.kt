package com.angeldevtech.gol.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.angeldevtech.gol.ui.screens.home.HomeScreen
import com.angeldevtech.gol.ui.screens.player.PlayerScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Home){
        composable<Home> {
            HomeScreen(onItemSelected = { scheduleItem ->
                navController.navigate(Player(scheduleItem.id))
            })
        }
        composable<Player> {
            PlayerScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}