package com.superacm.demo.settings.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.superacm.demo.lib.core.compose.FONT_SIZE_SMALL
import com.superacm.demo.lib.core.compose.TitleBar
import com.superacm.demo.lib.core.compose.sdp
import com.superacm.demo.settings.SettingUIState

const val ROUTE_VOLUME = "settings_volume"

fun NavGraphBuilder.settingsVolumeScreen(
    modifier: Modifier = Modifier,
    uiState: SettingUIState,
    onBack: () -> Unit
) {
    composable(route = ROUTE_VOLUME) {
        SettingsVolume(
            modifier = modifier,
            uiState = uiState,
            onBack = onBack
        )
    }
}

fun NavController.navigateToVolume() = navigate(ROUTE_VOLUME)

@Composable
fun SettingsVolume(
    modifier: Modifier = Modifier,
    uiState: SettingUIState,
    onBack: () -> Unit
) {
    Column(modifier = modifier) {
        Box(modifier = Modifier.background(color = Color.White)) {
            TitleBar(titleText = "声音设置", onBack = onBack)
        }
        Spacer(modifier = Modifier.height(18.dp))

        SeekItem(
            name = "通话音量设置",
            tips = "音量越大，对方听到的声音越大",
            propertyValue = if (uiState is SettingUIState.Success) uiState.model.volume else 0
        ) {

        }
    }


}


@Composable
private fun SeekItem(
    name: String,
    tips: String,
    propertyValue: Int,
    onValueChangeFinished: (Float) -> Unit
) {


    val initValue = propertyValue / 100f

    var progress by remember(initValue) {
        mutableFloatStateOf(initValue)
    }

    SettingsGroup {
        Column(modifier = Modifier.padding(30.sdp)) {
            Text(
                textAlign = TextAlign.Center,
                text = name,
                fontSize = FONT_SIZE_SMALL,
                color = Color(0xFF131033)
            )
            Text(
                textAlign = TextAlign.Center,
                text = tips,
                fontSize = FONT_SIZE_SMALL,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(18.sdp))
            SliderWithLabel(
                value = progress,
                valueRange = (0.01f).rangeTo(1.0f),
                onValueChange = {
                    progress = if (it <= 0.01) 0.01f else it
                },
                onValueChangeFinished = {
                    onValueChangeFinished.invoke(progress)
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFF5F2AD1),
                    inactiveTrackColor = Color(0xFFE8E8EF)
                ),
                showTopLabel = true,
                showBottomLabel = true
            )
        }
    }
}