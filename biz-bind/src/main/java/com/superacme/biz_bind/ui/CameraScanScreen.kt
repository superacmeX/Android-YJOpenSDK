package com.superacme.biz_bind.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import com.superacme.biz_bind.R
import com.superacme.biz_bind.ui.components.BindDeviceTitle
import com.superacme.biz_bind.ui.components.CameraPermissionWrapper
import com.superacme.biz_bind.ui.components.QRCodeScannerWithAnimation

@Composable
fun CameraScanScreen(
    onBackClick: () -> Unit = {},
    onBarcodeScanned: (String) -> Unit = {},
) {

    // 物理返回键
    BackHandler(onBack = onBackClick)

    Column {

        // 顶部标题
        BindDeviceTitle(title = stringResource(id = R.string.qr_scan_title)) {
            onBackClick.invoke()
        }

        // 约束布局
        ConstraintLayout {
            val (tips, finish) = createRefs()

            Box(modifier = Modifier
                .fillMaxSize()
                .constrainAs(tips) {
                    top.linkTo(parent.top)
                })
            {
                CameraPermissionWrapper {
                    QRCodeScannerWithAnimation(onBarcodeScanned = onBarcodeScanned)
                }
            }
        }
    }
}


@Composable
@Preview(showSystemUi = true, showBackground = true)
fun PreviewCameraScanScreen() {
    CameraScanScreen()
}

