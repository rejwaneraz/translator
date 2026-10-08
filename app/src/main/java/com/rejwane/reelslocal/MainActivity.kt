package com.rejwane.reelslocal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rejwane.reelslocal.ui.MainViewModel
import com.rejwane.reelslocal.ui.navigation.ReelsLocalNavHost
import com.rejwane.reelslocal.ui.theme.ReelsLocalTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            ReelsLocalTheme(themeMode = state.themeMode) {
                ReelsLocalNavHost(mainState = state)
            }
        }
    }
}
