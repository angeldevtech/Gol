package com.angeldevtech.gol.utils

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class DeviceTypeProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val isTvDevice: Boolean by lazy {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION ||
                !context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
    }
}