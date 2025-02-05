package com.superacm.demo.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.superacm.demo.lib.core.theme.DemoTheme

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deviceId = intent.getStringExtra("deviceId")

        setContent {
            DemoTheme { // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFEEEEEE)
                ) {
                    val viewModel: SettingsViewModel =
                        viewModel(factory = SettingsViewModel.provideFactory(deviceId = deviceId!!))

                    SettingsScreen(
                        modifier = Modifier.fillMaxSize(),
                        onBack = { finish() },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}