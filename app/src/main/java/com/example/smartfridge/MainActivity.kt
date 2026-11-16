package com.example.smartfridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartfridge.ui.navigation.AppNavGraph
import com.example.smartfridge.ui.screens.fridge.FridgeViewModel
import com.example.smartfridge.ui.theme.SmartFridgeTheme
import com.example.smartfridge.worker.ExpiryCheckWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 啟動每天 WorkManager 背景食材到期掃描推播
        ExpiryCheckWorker.scheduleDailyCheck(this)

        setContent {
            val fridgeViewModel: FridgeViewModel = viewModel()
            val uiState by fridgeViewModel.uiState.collectAsState()

            SmartFridgeTheme(isSeniorMode = uiState.isSeniorMode) {
                AppNavGraph()
            }
        }
    }
}
