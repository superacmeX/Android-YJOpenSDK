package com.superacme.login.viewcomponent

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.acme.login.R
import com.superacme.login.view.sdp

@Composable
fun SMLoginCheckBox(
    modifier: Modifier = Modifier
        .padding(all = 0.dp)
        .size(32.sdp),
    checked: Boolean = false,
    onClick: () -> Unit = {},
) {

    val imageVector =
        if (checked) R.drawable.sm_login_checkbox_checked else R.drawable.sm_login_checkbox_uncheck

    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Image(
            modifier = Modifier
                .wrapContentWidth(),
            painter = painterResource(imageVector),
            contentDescription = null
        )
    }
}