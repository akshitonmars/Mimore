package com.debroglie.mimore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.debroglie.mimore.ui.CalculatorScreen
import com.debroglie.mimore.ui.MimoreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: CalculatorViewModel = viewModel()
            MimoreTheme(vm.settings.themeMode, vm.settings.accent) {
                val view = LocalView.current
                val systemDark = isSystemInDarkTheme()
                val dark = when (vm.settings.themeMode) {
                    com.debroglie.mimore.data.ThemeMode.SYSTEM -> systemDark
                    com.debroglie.mimore.data.ThemeMode.DARK -> true
                    com.debroglie.mimore.data.ThemeMode.LIGHT -> false
                }
                SideEffect {
                    val controller = WindowCompat.getInsetsController(window, view)
                    controller.isAppearanceLightStatusBars = !dark
                    controller.isAppearanceLightNavigationBars = !dark
                }
                CalculatorScreen(vm)
            }
        }
    }
}
