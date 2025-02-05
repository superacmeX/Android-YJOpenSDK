package com.superacme.biz_bind.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superacme.biz_bind.core.Color33DEE2ED
import com.superacme.biz_bind.core.ColorFF131033
import com.superacme.biz_bind.core.ColorFF9B9DB1
import com.superacme.biz_bind.core.sdp

@Composable
fun InputLayout(
    placeTitle: String,
    inputValue: String,
    onValueChange: (String) -> Unit = {}
) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .height(108.sdp)
            .border(
                border = BorderStroke(1.dp, color = Color33DEE2ED),
                shape = RoundedCornerShape(20.sdp)
            ),
        singleLine = true,
        // 显示文本
        value = inputValue,
        // 监听文本变化，并赋值给text
        onValueChange = onValueChange,
        textStyle = TextStyle(
            fontSize = 16.sp, fontWeight = FontWeight.Normal, color = ColorFF131033
        ),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        placeholder = {
            Text(
                text = placeTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = ColorFF9B9DB1,
            )
        },
        // 设置键盘
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Next, keyboardType = KeyboardType.Text
        ),
    )
}