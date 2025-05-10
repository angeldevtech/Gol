package com.angeldevtech.gol.ui

import androidx.compose.material3.MaterialTheme as MobileMaterialTheme
import androidx.compose.material3.darkColorScheme as mobileDarkColorScheme
import androidx.compose.runtime.Composable
import com.angeldevtech.gol.utils.DeviceTypeProvider
import androidx.tv.material3.MaterialTheme as TvMaterialTheme
import androidx.tv.material3.darkColorScheme as tvDarkColorScheme

@Composable
fun Theme(
    deviceTypeProvider: DeviceTypeProvider,
    content: @Composable () -> Unit
) {
    if (deviceTypeProvider.isTvDevice) {
        TvMaterialTheme(
            colorScheme = tvDarkColorScheme(),
            typography = TvMaterialTheme.typography,
            content = content
        )
    } else {
        MobileMaterialTheme(
            colorScheme = mobileDarkColorScheme(),
            typography = MobileMaterialTheme.typography,
            content = content
        )
    }
}