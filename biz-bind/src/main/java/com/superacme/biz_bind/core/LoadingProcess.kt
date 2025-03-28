package com.superacme.biz_bind.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun LoadingProcess(textId: Int = -1) {
    CommonAlertDialog {
        ProcessLoading(text = if (textId != -1) stringResource(id = textId) else "")
    }
}

@Composable
fun CommonAlertDialog(content: @Composable () -> Unit) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(20.sdp),
            color = Color.Transparent,
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    content()
                }
            }
        }
    }
}

@Composable
fun ProcessLoading(modifier: Modifier = Modifier, text: String) {
    Row(
        modifier = modifier
            .wrapContentSize()
            .background(
                Color(0xff131033),
                shape = RoundedCornerShape(54.sdp)
            )
            .padding(
                vertical = 14.sdp,
                horizontal = 32.sdp
            ), verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 3.sdp,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .height(36.sdp)
                .width(36.sdp)
        )
        if (text.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 16.sdp),
                text = text,
                style = TextStyle(color = Color.White, fontSize = 16.sp)
            )
        }
    }
}