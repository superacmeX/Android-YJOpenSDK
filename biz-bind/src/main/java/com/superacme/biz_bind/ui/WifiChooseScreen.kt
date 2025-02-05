package com.superacme.biz_bind.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import com.superacme.biz_bind.R
import com.superacme.biz_bind.core.sdp
import com.superacme.biz_bind.ui.components.BindDeviceTitle
import com.superacme.biz_bind.ui.components.BottomActionBtn
import com.superacme.biz_bind.ui.components.InputLayout

@Composable
fun WifiChooseScreen(
    wifiSsid: String = "",
    ssidOnValueChange: (String) -> Unit = {},
    wifiPwd: String = "",
    pwdOnValueChange: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
) {

    // 物理返回键
    BackHandler(onBack = onBackClick)

    Column {

        // 顶部标题
        BindDeviceTitle(title = stringResource(id = R.string.wifi_bind_select_wifi)) {
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

                InputLayout(
                    placeTitle = stringResource(id = R.string.input_wifi_name),
                    inputValue = wifiSsid,
                    onValueChange = ssidOnValueChange
                )

                Spacer(modifier = Modifier.height(32.sdp))

                InputLayout(
                    placeTitle = stringResource(id = R.string.input_wifi_pwd_name),
                    inputValue = wifiPwd,
                    onValueChange = pwdOnValueChange
                )
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
fun PreviewWifiChooseScreen() {
    WifiChooseScreen()
}

