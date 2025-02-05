package com.superacm.demo.settings.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.superacm.demo.lib.core.compose.FONT_SIZE_MENU_SMALL
import com.superacm.demo.lib.core.compose.sdp
import kotlin.math.roundToInt


@Composable
fun SliderWithLabel(
    value: Float,
    sliderLeftIcon: @Composable BoxScope.() -> Unit = {},
    sliderRightIcon: @Composable BoxScope.() -> Unit = {},
    sliderTopWidget: @Composable ((modifier: Modifier) -> Boolean)? = null,
    valueRange: ClosedFloatingPointRange<Float>,
    labelMinWidth: Dp = 34.dp,
    colors: SliderColors = SliderDefaults.colors(),
    sliderLeftIconWidth: Dp = 0.dp,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    showTopLabel: Boolean = true,
    showBottomLabel: Boolean = false
) {
    Column {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            val thumbRadius = 10.dp //according to Slider.kt
            val offset = getSliderOffset(
                value = value,
                valueRange = valueRange,
                boxWidth = maxWidth,
                sliderPadding = thumbRadius + thumbRadius,
                sliderLeftIconWidth = sliderLeftIconWidth,
            )

            val fl = value * 100

            val endValueText = fl.roundToInt().toString()

            if (value >= valueRange.start && showTopLabel) {
                SliderLabel(
                    sliderTopWidget = sliderTopWidget,
                    label = endValueText,
                    minWidth = labelMinWidth,
                    modifier = Modifier
                        .offset(x = offset + sliderLeftIconWidth + thumbRadius)
                )
            }

        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = sliderLeftIconWidth, height = 50.sdp)
                    .align(Alignment.CenterVertically)
            ) {
                sliderLeftIcon()
            }

            Column(
                modifier = Modifier
                    .height(50.sdp)
                    .weight(1f)
            ) {
                Slider(
                    value = value,
                    colors = colors,
                    onValueChange = onValueChange,
                    valueRange = valueRange,
                    onValueChangeFinished = onValueChangeFinished,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Box(
                modifier = Modifier
                    .size(width = sliderLeftIconWidth, height = 50.sdp)
                    .align(Alignment.CenterVertically)
            ) {
                sliderRightIcon()
            }
        }

        if (showBottomLabel) {
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RangeLabel(label = "${(valueRange.start * 100).toInt()}")
                RangeLabel(modifier = Modifier.weight(1f), label = "50")
                RangeLabel(label = "${(valueRange.endInclusive * 100).toInt()}")
            }
        }

    }
}

@Composable
private fun RangeLabel(
    modifier: Modifier = Modifier,
    label: String
) {
    Text(
        modifier = modifier,
        text = label,
        textAlign = TextAlign.Center,
        color = Color(0XFF9B9DB1),
    )
}


@Preview
@Composable
fun PreviewSliderWithLabel() {
    Box(modifier = Modifier.background(Color.White)) {
        SliderWithLabel(
            0.5f, valueRange = 0f.rangeTo(1.0f), onValueChange = {},
            sliderLeftIconWidth = 41.sdp,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF5F2AD1),
                inactiveTrackColor = Color(0xFFE8E8EF)
            ),
            showTopLabel = true
        )
    }
}

@Composable
fun SliderLabel(
    label: String, minWidth: Dp, modifier: Modifier = Modifier,
    sliderTopWidget: @Composable ((modifier: Modifier) -> Boolean)? = null,
) {
    if (sliderTopWidget == null) {
        TopLabel(modifier = modifier, value = label)
    } else {
        sliderTopWidget(modifier)
    }
}

@Composable
private fun TopLabel(modifier: Modifier, value: String) {
    Box(
        modifier = modifier
            .size(width = 49.sdp, height = 52.sdp)
            .background(
                color = Color(0xFFB29CE1),
                shape = RoundedCornerShape(topStart = 10.sdp, topEnd = 10.sdp, bottomEnd = 10.sdp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = value, color = Color.White, fontSize = FONT_SIZE_MENU_SMALL)
    }
}


private fun getSliderOffset(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    boxWidth: Dp,
    sliderPadding: Dp,
    sliderLeftIconWidth: Dp,
): Dp {

    val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
    val positionFraction = calcFraction(valueRange.start, valueRange.endInclusive, coerced)

    return (boxWidth - sliderPadding - sliderLeftIconWidth) * positionFraction
}


// Calculate the 0..1 fraction that `pos` value represents between `a` and `b`
private fun calcFraction(a: Float, b: Float, pos: Float) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)