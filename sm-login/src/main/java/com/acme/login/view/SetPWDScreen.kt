package com.acme.login.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.acme.common.account.login.RequestSMSCodeType
import com.acme.login.util.LoginLogger
import com.acme.login.util.PWDUtils
import com.acme.login.util.getStringWhenNeedPreview
import com.acme.login.util.getStringWhenNeedPreview2
import com.superacme.login.view.BackButton
import com.superacme.login.view.LoginButton
import com.superacme.login.view.PwdInputText
import com.superacme.login.view.bgBrash
import com.superacme.login.view.sdp
import com.superacme.login.view.textStyleBlack56
import com.superacme.login.view.textStyleGray28
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.acme.login.R


fun pwdValid(input: String): Boolean {
    return PWDUtils.pwdValid(input)
}

fun originalPwdValid(input: String): Boolean {
    return input.isNotEmpty() && input.length >= 6
}

@Preview()
@Composable
fun PreviewSetPWDScreen(): Unit {
    SetPWDScreen(RequestSMSCodeType.FORGETPASS) {}
}

@Preview()
@Composable
fun PreviewSetPWDScreen2(): Unit {
    SetPWDScreen(RequestSMSCodeType.RESET_PWD) {}
}

@Preview()
@Composable
fun PreviewSetPWDScreen3(): Unit {
    SetPWDScreen(RequestSMSCodeType.SIGN_UP) {}
}

@Composable
fun SetPWDScreen(
    requestType: RequestSMSCodeType,
    onEvent: (LoginEvent) -> Unit
) {
    Surface(
        modifier = Modifier
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .background(brush = bgBrash)
                    .padding(start = pageMarginLeft, end = pageMarginLeft, top = 264.sdp)
            ) {
                val titleId = when (requestType) {
                    RequestSMSCodeType.SIGN_UP -> R.string.sm_login_set_pwd
                    RequestSMSCodeType.FORGETPASS -> R.string.sm_login_reset_pwd
                    RequestSMSCodeType.RESET_PWD -> R.string.sm_login_modify_pwd
                    else -> R.string.sm_login_set_pwd
                }
                val showPWD1ErrorTip = remember {
                    mutableStateOf(false)
                }
                val showPWD2ErrorTip = remember {
                    mutableStateOf(false)
                }
                Text(
                    text = getStringWhenNeedPreview(id = titleId),
                    color = Color.Black,
                    style = textStyleBlack56,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.sdp))

                Text(
                    text = getStringWhenNeedPreview(R.string.sm_pwd_tips),
                    color = Color.Gray,
                    style = textStyleGray28,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                val pwdText = remember { mutableStateOf("") }
                val confirmPwdText = remember { mutableStateOf("") }

                val oldPwdText = remember { mutableStateOf("") }
                val showOldPWDErrorTip = remember {
                    mutableStateOf(false)
                }

                val showOriginalPwdInput = requestType == RequestSMSCodeType.RESET_PWD

                if (showOriginalPwdInput) {
                    PwdInputText(
                        modifier = Modifier.padding(top = 72.sdp),
                        pwdState = oldPwdText,
                        inputMaxLength = 16,
                        hintStrId = R.string.sm_login_password_origin,
                        focusChange = {

                        },
                        onValueChanged = { pwd ->
                            LoginLogger.log("showOldPWD focus change $pwd")
                        })

                    Spacer(modifier = Modifier.height(30.sdp))
                }

                val topPadding = if (showOriginalPwdInput) 2.sdp else 72.sdp

                val firstPWDHint =
                    if (showOriginalPwdInput || requestType == RequestSMSCodeType.FORGETPASS) R.string.sm_login_new_password_hint else R.string.sm_login_new_password_hint
                PwdInputText(
                    modifier = Modifier.padding(top = topPadding),
                    pwdState = pwdText,
                    inputMaxLength = 16,
                    hintStrId = firstPWDHint,
                    focusChange = {


                    },
                    onValueChanged = { pwd ->

                    })

                Spacer(modifier = Modifier.height(30.sdp))

                val secondPWDHint =
                    if (showOriginalPwdInput || requestType == RequestSMSCodeType.FORGETPASS) R.string.sm_login_new_again_password_hint else R.string.sm_login_new_again_password_hint

                PwdInputText(
                    modifier = Modifier.padding(top = 2.sdp),
                    pwdState = confirmPwdText,
                    inputMaxLength = 16,
                    hintStrId = secondPWDHint,
                    focusChange = {

                    },
                    onValueChanged = { pwd ->

                    })
                
                Spacer(modifier = Modifier.height(48.sdp))

                val loginButtonEnable = if (showOriginalPwdInput)
                    oldPwdText.value.isNotEmpty() || pwdText.value.isNotEmpty() || confirmPwdText.value.isNotEmpty()
                else pwdText.value.isNotEmpty() || confirmPwdText.value.isNotEmpty()

                val coroutineScope = rememberCoroutineScope()
                val context = LocalContext.current
                LoginButton(
                    enable = loginButtonEnable,
                    text = getStringWhenNeedPreview(R.string.sm_login_confirm)
                ) {
                    val pwdHasEmptyOne =
                        if (showOriginalPwdInput) pwdText.value.isEmpty() || confirmPwdText.value.isEmpty() || oldPwdText.value.isEmpty() else pwdText.value.isEmpty() || confirmPwdText.value.isEmpty()

                    if (pwdHasEmptyOne) {
                        val str =
                            getStringWhenNeedPreview2(context, id = R.string.sm_login_pwd_please_input)
                        coroutineScope.launch(Dispatchers.Main) {
                            Toasts.showToast(str)
                        }
                        return@LoginButton
                    }
                    
                    var invalidPwd = false
                    if (showOriginalPwdInput) {
                        if (!pwdValid(oldPwdText.value)) {
                            invalidPwd = true
                        }
                    }

                    if (!pwdValid(pwdText.value)) {
                        invalidPwd = true
                    }

                    if (invalidPwd) {
                        val str =
                            getStringWhenNeedPreview2(context, id = R.string.sm_login_pwd_malformed)
                        coroutineScope.launch(Dispatchers.Main) {
                            Toasts.showToast(str)
                        }
                        return@LoginButton
                    }

                    val hasDifferentPwd = pwdText.value != confirmPwdText.value
                    if (hasDifferentPwd) {
                        val str = getStringWhenNeedPreview2(
                            context,
                            id = R.string.sm_login_set_pwd_two_pwd_different
                        )
                        coroutineScope.launch(Dispatchers.Main) {
                            Toasts.showToast(str)
                        }
                        return@LoginButton
                    }

                    if (showOriginalPwdInput) {
                        onEvent(LoginEvent.ResetPWD(oldPwdText.value, confirmPwdText.value))
                    } else {
                        onEvent(LoginEvent.SetPWD(pwdText.value, confirmPwdText.value))
                    }
                }
            }

            Box(modifier = Modifier.padding(start = 39.sdp, top = 87.sdp)) {
                BackButton { onEvent(LoginEvent.BackPressed) }
            }
        }
    }
}

@Composable
private fun GetErrDes(pwd: String) =
    if (pwd.length > PWDUtils.upperBound) {
        getStringWhenNeedPreview(id = R.string.sm_login_pwd_length_should_less_than_20)
    } else {
        if (pwd.length >= PWDUtils.lowerBound) getStringWhenNeedPreview(R.string.sm_login_pwd_malformed) else getStringWhenNeedPreview(
            R.string.sm_login_pwd_length_should_greater_eight
        )
    }


@Composable
private fun GetErrDesForOriginalPWD(pwd: String) =
    getStringWhenNeedPreview(R.string.sm_login_pwd_length_should_greater_six)


