package com.superacme.login.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.acme.common.account.login.RequestSMSCodeType
import com.acme.login.R
import com.acme.login.view.LoginEvent
import com.acme.login.view.PrivacyAgreementCheckBox
import com.acme.login.view.VerifyCodeInputData
import com.acme.login.viewcomponent.ProcessLoading

@Preview
@Composable
fun PreviewPhoneSignUpScreen() {
    PhoneSignUpScreen {}
}

@Composable
fun PhoneSignUpScreen(
    userName:String = "",
    showProgressBar: Boolean = false,
    currentRegionCode: String? = "US",
    onValueChange: (String) -> Unit = {},
    onClearButtonClick: () -> Unit = {},
    onEvent: (LoginEvent) -> Unit
) {
    Surface(modifier = Modifier) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .background(brush = bgBrash)
                    .padding(start = 56.sdp, end = 56.sdp, top = 264.sdp)
            ) {
                Text(
                    text = stringResource(id = R.string.sm_login_reg_title),
                    style = textStyleBlack56,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                val phoneOrEmailState by rememberSaveable(stateSaver = POEStateSaver) {
                    mutableStateOf(PhoneOrEmailState().also {
                        it.text = userName
                    })
                }

                val checkBoxState = rememberSaveable {
                    mutableStateOf(false)
                }

                val onSubmit = {
                    if (!phoneOrEmailState.isValid) {
                        onEvent(LoginEvent.PhoneOrEmailInvalid)
                    } else {
                        if (!checkBoxState.value) {
                            onEvent(LoginEvent.AlertPrivacyNotCheck)
                        } else {
                            val codeData = if (!phoneOrEmailState.isEmail()) {
                                VerifyCodeInputData(
                                    phone = phoneOrEmailState.text,
                                    requestType = RequestSMSCodeType.SIGN_UP
                                )
                            } else {
                                VerifyCodeInputData(
                                    email = phoneOrEmailState.text,
                                    requestType = RequestSMSCodeType.SIGN_UP
                                )
                            }

                            onEvent(LoginEvent.VerifySMSCode(codeData))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(72.sdp))

                PhoneOrEmail(
                    showKeyBord = true,
                    phoneOrEmailState = phoneOrEmailState,
                    imeAction = ImeAction.Done,
                    onImeAction = onSubmit,
                    onClearButtonClick = onClearButtonClick,
                    onValueChange = onValueChange
                )

                LoginButton(
                    modifier = Modifier.padding(top = 48.sdp),
                    enable = phoneOrEmailState.text.isNotEmpty(),
                    text = stringResource(R.string.sm_login_register),
                    onClick = onSubmit
                )

                PrivacyAgreementCheckBox(checkBoxState)
            }
            if (showProgressBar) {
                ProcessLoading(modifier = Modifier.align(Alignment.Center))
            }
            Box(modifier = Modifier.padding(start = 39.sdp, top = 87.sdp)) {
                BackButton { onEvent(LoginEvent.BackPressed) }
            }
        }
    }
}