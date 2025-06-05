package com.angeldevtech.gol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.angeldevtech.gol.navigation.AppNavHost
import com.angeldevtech.gol.ui.Theme
import com.angeldevtech.gol.utils.DeviceTypeProvider
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

/**
 * Loads [AppNavHost].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var deviceTypeProvider: DeviceTypeProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Theme(deviceTypeProvider) {
                AppNavHost(deviceTypeProvider)
            }
        }
    }
}