package com.superacm.demo.lib.core.compose

import androidx.compose.ui.unit.Dp

val Int.sdp: Dp get() = Dp(value = scaleDp(this.toFloat()))

fun scaleDp(dp: Float): Float {
    return dp / 2
}
