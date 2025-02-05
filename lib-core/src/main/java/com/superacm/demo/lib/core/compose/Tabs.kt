package com.superacm.demo.lib.core.compose

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.TextUnit
import com.superacm.demo.lib.core.R

@Composable
fun Tab(
    modifier: Modifier = Modifier,
    text: String,
    fontSize: TextUnit,
    color: Color
) {
    Text(
        modifier = modifier,
        text = text,
        fontSize = fontSize,
        color = color
    )
}


@Composable
fun TabName(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = colorFF131033
) {
    Tab(
        modifier = modifier,
        text = text,
        fontSize = FONT_SIZE_SMALL,
        color = color
    )
}

@Composable
fun TabSubName(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = colorAA131033
) {
    Tab(
        modifier = modifier,
        text = text,
        fontSize = FONT_SIZE_MENU_SMALL,
        color = color
    )
}

@Composable
fun TabValue(
    modifier: Modifier = Modifier,
    text: String
) {

    Tab(modifier = modifier, text = text, fontSize = FONT_SIZE_SMALL, color = colorAA9B9DB1)

}

@Composable
fun TabNext() {
    Icon(
        imageVector = ImageVector.vectorResource(R.drawable.sm_angle_right_bracket_icon),
        contentDescription = "desc",
        modifier = Modifier
            .height(20.sdp)
            .width(12.sdp),
        tint = colorAA9B9DB1,
    )
}