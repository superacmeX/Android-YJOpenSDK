package com.superacm.demo.settings.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.compose.AsyncImage
import com.superacm.demo.lib.core.compose.FONT_SIZE_SMALL
import com.superacm.demo.lib.core.compose.TitleBar
import com.superacm.demo.lib.core.compose.sdp
import com.superacm.demo.settings.R
import com.superacm.demo.settings.SettingUIState

const val SETTINGS_MAIN = "settings_main"


fun NavGraphBuilder.settingMainScreen(
    uiState: SettingUIState,
    navigate: (String) -> Unit,
    onReboot: () -> Unit,
    onBack: () -> Unit
) {
    composable(route = SETTINGS_MAIN) {
        SettingsMain(
            uiState = uiState,
            navigate = navigate,
            onBack = onBack,
            onReboot = onReboot
        )
    }
}


@Composable
fun SettingsMain(
    uiState: SettingUIState,
    navigate: (String) -> Unit,
    onReboot: () -> Unit,
    onBack: () -> Unit
) {

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.background(color = Color.White)) {
            TitleBar(titleText = "设置", onBack = onBack)
        }
        Spacer(modifier = Modifier.height(18.dp))

        if (uiState is SettingUIState.Success) {
            DeviceSettingTitle(uiState.model)
        }

        Spacer(modifier = Modifier.height(18.dp))

        SettingsGroup {
            SettingItemUI(
                icon = R.drawable.setting_voice,
                name = "声音设置",
                onItemClick = { navigate.invoke(ROUTE_VOLUME) })
        }
        Spacer(modifier = Modifier.height(18.dp))

        SettingsGroup {
            SettingButton(
                icon = R.drawable.device_setting_reboot,
                text = "重启设备",
                color = Color.Black,
                onItemClick = onReboot
            )
        }
        Spacer(modifier = Modifier.height(18.dp))

        SettingsGroup {
            SettingButton(
                text = "删除设备",
                color = Color.Red,
                onItemClick = { navigate.invoke(ROUTE_VOLUME) })
        }
    }

}

@Composable
fun SettingItemUI(
    icon: Int = -1,
    name: String,
    tips: String = "",
    route: String = "",
    onItemClick: (String) -> Unit,
    showArrow: Boolean = true
) {

    Row(
        modifier = Modifier
            .height(108.sdp)
            .clip(RoundedCornerShape(20.sdp))
            .clickable {
                onItemClick.invoke(route)
            }
            .padding(
                start = 30.sdp,
                end = 30.sdp
            ),
        verticalAlignment = Alignment.CenterVertically) {

        if (icon > 0) {
            AsyncImage(
                model = icon,
                contentDescription = null,
                modifier = Modifier.size(40.sdp),
                contentScale = ContentScale.Inside
            )
            Spacer(modifier = Modifier.width(16.sdp))
        }
        Text(
            text = name,
            fontSize = FONT_SIZE_SMALL,
            color = Color(0XFF131033)
        )

        Spacer(modifier = Modifier.weight(1f))

        if (tips.isNotEmpty()) {
            Text(
                text = tips,
                fontSize = FONT_SIZE_SMALL,
                color = Color(0xAA9B9DB1),
                modifier = Modifier.padding(
                    start = 10.sdp,
                    end = 34.sdp
                )
            )
        }
        if (showArrow) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.sm_angle_right_bracket_icon),
                contentDescription = "desc",
                modifier = Modifier
                    .height(20.sdp)
                    .width(12.sdp),
                tint = Color(0xff9B9DB1),
            )
        }

    }

}

@Composable
fun SettingButton(icon: Int = -1, text: String, color: Color, onItemClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(108.sdp)
            .clip(RoundedCornerShape(20.sdp))
            .clickable {
                onItemClick.invoke()
            }
            .padding(
                start = 30.sdp,
                end = 30.sdp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        if (icon > -1) {
            AsyncImage(
                model = icon,
                contentDescription = null,
                modifier = Modifier.size(32.sdp)
            )
            Spacer(modifier = Modifier.width(16.sdp))
        }

        Text(text = text, fontSize = FONT_SIZE_SMALL, color = color)

    }
}


