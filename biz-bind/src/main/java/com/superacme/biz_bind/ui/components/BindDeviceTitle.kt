package com.superacme.biz_bind.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.superacme.biz_bind.R
import com.superacme.biz_bind.core.ColorFF131033
import com.superacme.biz_bind.core.clickableWithoutIndication
import com.superacme.biz_bind.core.sdp

@Composable
fun BindDeviceTitle(
    title: String = "",
    backClick: () -> Unit = {}
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.sdp)
    ) {
        val (backImg, progress) = createRefs()

        Image(painter = painterResource(id = R.drawable.icon_black_back),
            contentDescription = null,
            modifier = Modifier
                .clickableWithoutIndication {
                    backClick.invoke()
                }
                .size(56.sdp)
                .constrainAs(backImg) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start, 28.sdp)
                    bottom.linkTo(parent.bottom)
                })

        Box(modifier = Modifier.constrainAs(progress) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
        }) {
            Text(
                text = title, fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = ColorFF131033
            )
//            BindDeviceStepProgress(current = currentStep, total = totalStep)
        }
    }
}


@Composable
@Preview(showBackground = true)
fun PreviewBindDeviceTitle() {
    BindDeviceTitle(title = "xxx")
}
