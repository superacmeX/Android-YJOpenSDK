package com.superacme.login.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.acme.common.account.login.RequestSMSCodeType
import com.acme.login.R
import com.acme.login.view.InnerLoginLogger
import com.acme.login.view.LoginEvent
import com.acme.login.view.VerifyCodeInputData
import com.acme.login.LoginActivity

private val logTag = "andymao->PhoneForgetPWDScreen"

@Composable
fun PhoneForgetPWDScreen(
    requestType: RequestSMSCodeType,
    scene: String? = null,
    lastUsrName: String,
    onValueChange: (String) -> Unit = {},
    onClearButtonClick: () -> Unit = {},
    onEvent: (LoginEvent) -> Unit
) {
    Surface {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .background(brush = bgBrash)
                    .padding(start = 56.sdp, end = 56.sdp, top = 264.sdp)
            ) {
                Text(
                    text = stringResource(getStringTitleId(scene)),
                    style = textStyleBlack56,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                val phoneOrEmailState by rememberSaveable(stateSaver = POEStateSaver) {
                    InnerLoginLogger.info("$logTag PhoneForgetPWDScreen() called rememberSaveable")
                    mutableStateOf(PhoneOrEmailState())
                }

                if (phoneOrEmailState.text.isEmpty() && !phoneOrEmailState.isFocusedDirty) {
                    var setLastUsrName by remember { mutableStateOf(false) }
                    if(setLastUsrName.not()) {
                        InnerLoginLogger.info("$logTag PhoneForgetPWDScreen() called set text to lastUsrName=$lastUsrName")
                        phoneOrEmailState.text = lastUsrName
                        setLastUsrName = true
                    }
                }

                val onSubmit = {
                    if (!phoneOrEmailState.isValid) {
                        onEvent(LoginEvent.PhoneOrEmailInvalid)
                    }else{
                        val codeData = if (!phoneOrEmailState.isEmail()) {
                            VerifyCodeInputData(
                                phone = phoneOrEmailState.text,
                                requestType = requestType
                            )
                        } else {
                            VerifyCodeInputData(
                                email = phoneOrEmailState.text,
                                requestType = requestType
                            )
                        }
                        onEvent(LoginEvent.VerifySMSCode(codeData))
                    }

                }

                VerticalLineSpacer(64)

                PhoneOrEmail(
                    phoneOrEmailState = phoneOrEmailState,
                    imeAction = ImeAction.Done,
                    onImeAction = onSubmit,
                    onClearButtonClick = {
                        InnerLoginLogger.info("$logTag PhoneForgetPWDScreen() called onClearButtonClick")
                        phoneOrEmailState.text = ""
                        onClearButtonClick()
                        InnerLoginLogger.info("$logTag PhoneForgetPWDScreen() called onClearButtonClick ${phoneOrEmailState.text}, phoneOrEmailState=${phoneOrEmailState.hashCode()}")
                    },
                    onValueChange = onValueChange
                )

                LoginButton(
                    modifier = Modifier.padding(top = 48.sdp),
                    enable = phoneOrEmailState.text.isNotEmpty(),
                    text = stringResource(id = R.string.sm_login_get_verify_code),
                    onClick = onSubmit,
                )
            }
            Box(modifier = Modifier.padding(start = 39.sdp, top = 87.sdp)) {
                BackButton { onEvent(LoginEvent.BackPressed) }
            }
        }
    }
}

fun getStringTitleId(scene: String?): Int {
    return when (scene) {
        LoginActivity.sceneChangePhone -> R.string.sm_login_forget_pwd_title2
        LoginActivity.sceneChangeEmail -> R.string.sm_login_forget_pwd_title3
        else -> R.string.sm_login_forget_pwd_title
    }
}

@Preview
@Composable
fun PhoneForgetPWDScreenPreview() {
    PhoneForgetPWDScreen(RequestSMSCodeType.RESET_PWD,"","") {}
}
