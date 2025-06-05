package com.angeldevtech.gol.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface as MobileSurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.Surface as TvSurface
import com.angeldevtech.gol.ui.screens.home.HomeScreen
import com.angeldevtech.gol.ui.screens.home.MobileHomeScreen
import com.angeldevtech.gol.ui.screens.player.MobilePlayerScreen
import com.angeldevtech.gol.ui.screens.player.PlayerScreen
import com.angeldevtech.gol.utils.DeviceTypeProvider

@Composable
fun AppNavHost(
    deviceTypeProvider: DeviceTypeProvider
) {
    val navController = rememberNavController()
    val isTv = deviceTypeProvider.isTvDevice

    if (isTv){
        TvSurface(
            modifier = Modifier.fillMaxSize(),
        ) {
            TvNavHost(navController)
        }
    } else {
        MobileSurface(
            modifier = Modifier.fillMaxSize()
        ) {
            MobileNavHost(navController)
        }
    }
}

@Composable
private fun TvNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Home) {
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

@Composable
private fun MobileNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Home) {
        composable<Home> {
            MobileHomeScreen(onItemSelected = { scheduleItem ->
                navController.navigate(Player(scheduleItem.id))
            })
        }
        composable<Player> {
            MobilePlayerScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}