package com.superacme.biz_bind.core

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier

fun Modifier.clickableWithoutIndication(onClick: () -> Unit): Modifier {
    return clickable(
        indication = null,
        interactionSource = MutableInteractionSource(),
        onClick = onClick
    )
}