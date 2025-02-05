package com.superacme.biz_bind.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.superacme.biz_bind.R
import com.superacme.biz_bind.core.ColorFF222222
import com.superacme.biz_bind.core.sdp
import com.superacme.biz_bind.ui.components.BindDeviceTitle
import com.superacme.biz_bind.ui.components.BottomActionBtn

@Composable
fun GuideResetScreen(
    tipsList: MutableList<String> = mutableListOf(),
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
) {

    // 物理返回键
    BackHandler(onBack = onBackClick)

    Column {

        // 顶部标题
        BindDeviceTitle(title = stringResource(id = R.string.wifi_bind_reset_camera)) {
            onBackClick.invoke()
        }

        // 约束布局
        ConstraintLayout {
            val (tips, finish) = createRefs()

            Column(modifier = Modifier
                .fillMaxSize()
                .padding(start = 56.sdp, end = 56.sdp)
                .constrainAs(tips) {
                    top.linkTo(parent.top)
                }
                .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.sdp)
                ) {
                    for (item in tipsList) {
                        Text(
                            text = item,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = ColorFF222222,
                            modifier = Modifier.padding(bottom = 18.sdp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(start = 56.sdp, end = 56.sdp)
                    .constrainAs(finish) { bottom.linkTo(parent.bottom) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                BottomActionBtn(
                    textResId = R.string.wifi_bind_next,
                    actionClick = { onNextClick.invoke() },
                    outTopPadding = 24.sdp,
                    outBottomPadding = 30.sdp,
                    isEnableClick = true
                )
            }
        }
    }
}


@Composable
@Preview(showSystemUi = true, showBackground = true)
fun PreviewGuideResetScreen() {
    val tipsList = mutableListOf<String>()
    tipsList.add(stringResource(id = R.string.bluetooth_device_reset_tips1))
    tipsList.add(stringResource(id = R.string.bluetooth_device_reset_tips2))
    tipsList.add(stringResource(id = R.string.bluetooth_device_reset_tips3))
    GuideResetScreen(tipsList = tipsList)
}

