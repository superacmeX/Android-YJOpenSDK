package com.superacm.demo.lib.core.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun Modifier.clickableWithoutIndication(onClick: () -> Unit): Modifier {
    return this.clickable(
        onClick = { onClick.invoke() },
        indication = null,
        interactionSource = remember {
            MutableInteractionSource()
        })
}