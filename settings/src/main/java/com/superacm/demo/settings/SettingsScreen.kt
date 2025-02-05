package com.superacm.demo.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.superacm.demo.settings.feature.SETTINGS_MAIN
import com.superacm.demo.settings.feature.settingMainScreen
import com.superacm.demo.settings.feature.settingsVolumeScreen


@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier) {

        SettingsNavigator(
            startDestination = SETTINGS_MAIN,
            onReboot = viewModel::onReboot,
            onBack = onBack,
            uiState = uiState
        )

    }

}


@Composable
fun SettingsNavigator(
    navController: NavHostController = rememberNavController(),
    startDestination: String,
    uiState: SettingUIState,
    onReboot: () -> Unit,
    onBack: () -> Unit,
) {
    NavHost(
        navController = navController, startDestination = startDestination
    ) {

        settingMainScreen(
            uiState = uiState,
            onBack = onBack,
            onReboot = onReboot,
            navigate = { navController.navigate(it) })

        settingsVolumeScreen(uiState = uiState) { navController.popBackStack() }

    }
}