package com.acme.login.view

import android.content.res.Resources
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.acme.common.account.logger.LoginLogger
import com.acme.common.account.login.RequestSMSCodeType
import com.acme.login.R
import com.acme.login.util.getStringWhenNeedPreview
import com.acme.login.viewcomponent.CodeTextField
import com.acme.login.vm.LoginState
import com.superacme.login.view.BackButton
import com.superacme.login.view.LoginButton
import com.superacme.login.view.bgBrash
import com.superacme.login.view.sdp
import com.superacme.login.view.textStyleBlack32
import com.superacme.login.view.textStyleBlack56
import com.superacme.login.view.textStyleGray32
import com.superacme.login.view.textStylePurple32

/**
 * 用于传递数据到验证码请求页面
 */
data class VerifyCodeInputData(
    val code: String? = null,
    val requestType: RequestSMSCodeType,
    val phone: String? = null,
    val phoneArea: String? = null,
    val email: String? = null
) : java.io.Serializable {
    companion object {
        private const val serialVersionUID = -2047L
    }
}

val pageMarginLeft = 56.sdp
val borderFocusColor = Color(0xff5F2AD1)
val borderColor = Color(0xffD9D9D9)
val boxBgColor = Color(0x08000000)

@Composable
fun VerifyCodeInputScreen(
    receiver: String,
    requestKey: String,
    loginState: LoginState,
    onEvent: (LoginEvent) -> Unit
) {
    Surface(
        modifier = Modifier
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = bgBrash)
                    .padding(start = pageMarginLeft, end = pageMarginLeft, top = 264.sdp)
                    .verticalScroll(rememberScrollState())
            ) {

                Text(
                    text = stringResource(id = R.string.sm_login_pls_type_code),
                    style = textStyleBlack56,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.sdp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.sm_login_code_send) + " ",
                        style = textStyleGray32,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        modifier = Modifier
                            .wrapContentWidth()
                    )

                    Text(
                        text = receiver,
                        style = textStyleBlack32,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(72.sdp))

                //when request key change, button should disable
                val nextButtonEnable = rememberSaveable(requestKey) {
                    LoginLogger.log("VerifyCodeInputScreen() nextButtonEnable rest to disable")
                    mutableStateOf(false)
                }

                var verifyCodeText by rememberSaveable(requestKey) {
                    LoginLogger.log("VerifyCodeInputScreen() verifyCodeText rest to empty")
                    mutableStateOf("")
                }

                val itemSize by remember {
                    val internalMargin = 8.dp
                    val itemSize = 6
                    val leftRightMargin = pageMarginLeft
                    val scale = Resources.getSystem().displayMetrics.density

                    val squareWidthDp =
                        (Resources.getSystem().displayMetrics.widthPixels / scale - (itemSize * internalMargin).value.toInt() - leftRightMargin.value * 2) / itemSize
                    mutableFloatStateOf(squareWidthDp)
                }

                // 验证码圆角输入框；自定义输入框颜色
                CodeTextField(
                    value = verifyCodeText,
                    boxWidth = itemSize.dp,
                    boxMargin = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth(),
                    onValueChange = {
                        verifyCodeText = it
                        if (verifyCodeText.length == 6) {
                            nextButtonEnable.value = true
                            onEvent(LoginEvent.SMSCodeCompleteEvent(verifyCodeText))
                        } else {
                            nextButtonEnable.value = false
                        }

                    },
                    cursorBrush = SolidColor(borderFocusColor),
                    boxShape = RoundedCornerShape(10.dp),
                    boxBackgroundColor = boxBgColor,
                    boxBorderStroke = BorderStroke(1.dp, color = borderColor),
                    boxFocusedBorderStroke = BorderStroke(2.dp, color = borderFocusColor),
                )


                TimeCountDownloadTextView(
                    remainSec = loginState.countTimeSeconds,
                    onRetry = {
                        verifyCodeText = ""
                    },
                    onEvent = onEvent
                )

                Spacer(modifier = Modifier.height(48.sdp))

                LoginButton(
                    enable = nextButtonEnable.value && !loginState.smsChecking,
                    text = getStringWhenNeedPreview(R.string.sm_login_next_step)
                ) {
                    onEvent(LoginEvent.SMSCodeCompleteEvent(verifyCodeText))
                }

            }

            if (loginState.smsChecking) {
//                ProcessLoading(modifier = Modifier.align(Center))
            }

            Box(modifier = Modifier.padding(start = 39.sdp, top = 87.sdp)) {
                BackButton { onEvent(LoginEvent.BackPressed) }
            }
        }
    }
}

@Composable
fun TimeCountDownloadTextView(
    remainSec: Int, onRetry: () -> Unit, onEvent: (LoginEvent) -> Unit
) {
    if (remainSec == 0) {
        Text(text = stringResource(id = R.string.sm_login_get_verify_code_again),
            style = textStylePurple32,
            modifier = Modifier
                .padding(top = 32.sdp)
                .wrapContentWidth()
                .clickable(true) {
                    onRetry()
                    onEvent(LoginEvent.RetryVerifyCode)
                })
    } else {
        val text = stringResource(id = R.string.sm_login_desc_of_can_require_again, remainSec)
        Text(
            text = text,
            style = textStyleGray32,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(top = 32.sdp)
                .wrapContentWidth()
        )
    }

}

@Preview
@Composable
private fun PreviewVerifyCodeInputScreen() {
    VerifyCodeInputScreen(receiver = "12222", requestKey = "abc", loginState = LoginState()) {

    }
}
