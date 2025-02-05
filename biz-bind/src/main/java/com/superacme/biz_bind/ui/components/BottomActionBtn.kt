package com.superacme.biz_bind.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.superacme.biz_bind.R
import com.superacme.biz_bind.core.Color335F2AD1
import com.superacme.biz_bind.core.Color338251EC
import com.superacme.biz_bind.core.ColorFF5F2AD1
import com.superacme.biz_bind.core.ColorFF8251EC
import com.superacme.biz_bind.core.ColorFFFFFFFF
import com.superacme.biz_bind.core.clickableWithoutIndication
import com.superacme.biz_bind.core.sdp

@Composable
fun BottomActionBtn(
    @StringRes textResId: Int,
    actionClick: () -> Unit = {},
    outTopPadding: Dp = 0.sdp,
    outBottomPadding: Dp = 0.sdp,
    isEnableClick: Boolean = false,
) {

    // 本地变量，背景选中态
    val bgGradientList: List<Color> = if (isEnableClick) {
        listOf(ColorFF8251EC, ColorFF5F2AD1)
    } else {
        listOf(Color338251EC, Color335F2AD1)
    }

    Text(
        text = stringResource(id = textResId),
        fontSize = 17.sp,
        fontWeight = FontWeight.Normal,
        color = ColorFFFFFFFF,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickableWithoutIndication {
                if (isEnableClick) {
                    actionClick.invoke()
                }
            }
            .padding(
                top = outTopPadding,
                bottom = outBottomPadding,
            )
            .background(
                brush = Brush.linearGradient(bgGradientList),
                shape = RoundedCornerShape(100.sdp)
            )
            .padding(
                top = 30.sdp,
                bottom = 30.sdp
            )
    )
}

@Composable
@Preview(showBackground = true)
fun PreviewBottomActionBtn() {
    BottomActionBtn(
        textResId = R.string.wifi_bind_next,
    )
}