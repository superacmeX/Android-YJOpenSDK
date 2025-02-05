package com.superacm.demo.lib.core.compose


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.superacm.demo.lib.core.R


@Composable
fun TitleBar(
    titleText: String,
    rightText: String? = null,
    onRight: (() -> Unit)? = null,
    onBack: () -> Unit,
) {
    TitleBar(
        left = {
            BackImage(onBack = onBack)
        },
        center = {
            TitleText(text = titleText)
        },
        right = {
            rightText?.let {
                TabName(
                    modifier = Modifier.clickableWithoutIndication {
                        onRight?.invoke()
                    },
                    text = rightText
                )
            }
        }
    )
}


@Composable
fun TitleBar(
    left: @Composable () -> Unit,
    center: @Composable () -> Unit,
    right: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 30.sdp, vertical = 10.sdp)
            .fillMaxWidth()
            .height(88.sdp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        left()
        Spacer(modifier = Modifier.weight(1f))
        center()
        Spacer(modifier = Modifier.weight(1f))
        right()
    }


}

@Composable
fun BackImage(modifier: Modifier = Modifier, onBack: () -> Unit) {
    Image(painter = painterResource(id = R.drawable.common_black_back),
        contentDescription = null,
        modifier = modifier
            .clickableWithoutIndication {
                onBack()
            }
            .width(10.5.dp)
            .height(21.dp))

}


@Composable
fun TitleText(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        text = text,
        fontSize = FONT_SIZE_BIG_TITLE,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        color = Color(0xFF222222)
    )
}