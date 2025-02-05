package com.acme.login.viewcomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.superacme.login.view.sdp
import com.superacme.login.view.stu

@Composable
fun ProcessLoading(modifier: Modifier = Modifier, text: String = "Loading") {
    Row(
        modifier =modifier
            .wrapContentSize()
            .background(
                Color(0xff131033),
                shape= RoundedCornerShape(54.sdp)
            )
            .padding(vertical=14.sdp,
                horizontal=32.sdp), verticalAlignment = Alignment.CenterVertically) {
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
                    .padding(start=16.sdp),
                text = text,
                style = TextStyle(color = Color.White, fontSize = 32.stu)
            )
        }
    }
}
