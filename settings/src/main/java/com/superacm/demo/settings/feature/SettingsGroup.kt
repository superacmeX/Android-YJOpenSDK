package com.superacm.demo.settings.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.superacm.demo.lib.core.compose.sdp

@Composable
fun SettingsGroup(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 30.sdp)
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(size = 20.sdp)
            )
    ) {
        content()
    }
}