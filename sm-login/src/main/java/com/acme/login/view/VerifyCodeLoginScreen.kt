package com.acme.login.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.acme.login.R
import com.acme.login.util.getStringWhenNeedPreview
import com.superacme.login.view.ColorGray
import com.superacme.login.view.ColorPurple
import com.superacme.login.view.LoginButton
import com.superacme.login.view.POEStateSaver
import com.superacme.login.view.PhoneOrEmail
import com.superacme.login.view.PhoneOrEmailState
import com.superacme.login.view.PwdInputText
import com.superacme.login.view.VerticalLineSpacer
import com.superacme.login.view.bgBrash
import com.superacme.login.view.sdp
import com.superacme.login.view.stu
import com.superacme.login.view.textStyleBlack28
import com.superacme.login.view.textStyleGray32
import com.superacme.login.viewcomponent.SMLoginCheckBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val logTag = "andymao->LoginScreen"

fun checkEmail(email: String): Boolean {
    var flag = false
    try {
        if (!email.contains("@")) {
            flag = false
            return false
        }
        val i = email.lastIndexOf("@")
        val count: Int = email.count { it == '@' }
        if (count > 1) {
            flag = false
            return false
        }

        val j = email.lastIndexOf(".")
        flag = if (i < j) {
            // 判断特殊情况@.xxx。返回false
            if (i + 1 == j) {
                false
            } else {
                true
            }
        } else {
            false
        }
    } catch (e: Exception) {
        flag = false
    }
    return flag
}
@Composable
fun LoginScreen(
    oneKeyLogin: MutableState<Boolean>,
    showLoading: MutableState<Boolean>,
    showVerifyCodeLogin: Boolean,
    lastUsrName: String,
    onValueChange: (String) -> Unit = {},
    onClearButtonClick: () -> Unit = {},
    onEvent: (LoginEvent) -> Unit
) {
    Box(modifier = Modifier.imePadding()) {
        Surface(modifier = Modifier) {
            val state = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(state)
                    .background(brush = bgBrash)
                    .padding(start = 56.sdp, end = 56.sdp, top = 130.sdp)
            ) {
                LoginTopViews(onEvent)
                InnerLoginLogger.info("$logTag LoginScreen() compose with: lastUsrName= $lastUsrName")
                val phoneOrEmailState by rememberSaveable(stateSaver = POEStateSaver, key = "showVerifyCodeLogin=$showVerifyCodeLogin$lastUsrName") {
                    InnerLoginLogger.info("$logTag LoginScreen() called with: lastUsrName= $lastUsrName")
                    mutableStateOf(PhoneOrEmailState().also {
                        it.text = lastUsrName
                    })
                }

                var setOnceLastUserName by remember {
                    mutableStateOf(false)
                }

                if(setOnceLastUserName.not() && lastUsrName.isNotEmpty()) {
                    setOnceLastUserName = true
                    InnerLoginLogger.info("$logTag LoginScreen() called with: lastUsrName= $lastUsrName, and setOnceLastUserName")
                    phoneOrEmailState.text = lastUsrName
                }

                val checkBoxState = rememberSaveable {
                    mutableStateOf(false)
                }

                val pwdState = rememberSaveable {
                    mutableStateOf("")
                }

                val coroutineScope = rememberCoroutineScope()
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val onSubmit = onSubmit@{
                        if (!phoneOrEmailState.isValid) {
                            onEvent(LoginEvent.PhoneOrEmailInvalid)
                            return@onSubmit
                        }

                        if (phoneOrEmailState.isEmail()) {
                            if (!checkEmail(phoneOrEmailState.text)) {
                                InnerLoginLogger.info("$logTag LoginScreen() called checkEmail failed ${phoneOrEmailState.text}")
                                onEvent(LoginEvent.PhoneOrEmailInvalid)
                                return@onSubmit
                            }
                        }

                        if (!showVerifyCodeLogin && (pwdState.value.isEmpty())) {
                            onEvent(LoginEvent.PwdInvalid(pwdState.value))
                            return@onSubmit
                        }

                        if (!checkBoxState.value) {
                            onEvent(LoginEvent.AlertPrivacyNotCheck)
                            coroutineScope.launch(Dispatchers.IO) {
                                state.scrollTo(Int.MAX_VALUE)
                            }
                            return@onSubmit
                        }

                        if (!showVerifyCodeLogin) {
                            onEvent(LoginEvent.PWDLogin(phoneOrEmailState.text, pwdState.value))
                        } else {
                            if (phoneOrEmailState.isEmail()) {
                                onEvent(LoginEvent.GetSMSCodeEvent(email = phoneOrEmailState.text))
                            } else {
                                onEvent(LoginEvent.GetSMSCodeEvent(phoneNum = phoneOrEmailState.text))
                            }
                        }
                    }

                    VerticalLineSpacer

                    PhoneOrEmail(
                        phoneOrEmailState = phoneOrEmailState,
                        imeAction = ImeAction.Done,
                        onImeAction = onSubmit,
                        onClearButtonClick = onClearButtonClick,
                        onValueChange = onValueChange
                    )

                    if (!showVerifyCodeLogin) {
                        PwdInputText(
                            modifier = Modifier.padding(top = 32.sdp),
                            pwdState = pwdState,
                            hintStrId = R.string.sm_login_password_hint
                        )

                        Text(text = getStringWhenNeedPreview(R.string.sm_login_forget_pwd),
                            style = textStyleBlack28,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .padding(top = 20.sdp)
                                .align(Alignment.End)
                                .wrapContentSize()
                                .clickable {
                                    onEvent(LoginEvent.ForgetPWD(phoneOrEmailState.text))
                                })

                    }

                    val buttonText =
                        if (showVerifyCodeLogin) getStringWhenNeedPreview(R.string.sm_login_require_code) else getStringWhenNeedPreview(
                            R.string.sm_login_login
                        )

                    val buttonEnable = remember {
                        derivedStateOf {
                            if (showVerifyCodeLogin) {
                                phoneOrEmailState.text.isNotEmpty()
                            } else {
                                phoneOrEmailState.text.isNotEmpty() || pwdState.value.isNotEmpty()
                            }
                        }
                    }

                    LoginButton(
                        modifier = Modifier.padding(top = 48.sdp),
                        enable = buttonEnable.value,
                        text = buttonText
                    ) { onSubmit() }

                    PrivacyAgreementCheckBox(checkBoxState)
                }

                BottomLinks(
                    oneKeyLogin = oneKeyLogin,
                    phoneOrEmailState = phoneOrEmailState,
                    showVerifyCodeLogin = showVerifyCodeLogin,
                    showPWDLoginLink = true,
                    onEvent = onEvent
                )
            }
        }
        if (showLoading.value) {
            //ProcessLoading(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun LoginTopViews1(onEvent: (LoginEvent) -> Unit) {
    Logo(
        modifier = Modifier
            .width(128.sdp)
            .height(128.sdp)
    )
    Text(
        text = getStringWhenNeedPreview(id = R.string.sm_login_welcome),
        color = Color.Black,
        style = TextStyle(
            color = Color.Black, fontSize = 56.stu
        ),
        textAlign = TextAlign.Start,
        modifier = Modifier
            .padding(top = 34.sdp)
            .fillMaxWidth()
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.sdp)
    ) {
        Text(
            text = getStringWhenNeedPreview(id = R.string.sm_login_register_tips1),
            style = textStyleGray32,
            textAlign = TextAlign.Start,
            modifier = Modifier.wrapContentWidth()
        )

        Text(text = getStringWhenNeedPreview(id = R.string.sm_login_register_tips2),
            style = TextStyle(
                color = ColorPurple, fontSize = 32.stu
            ),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEvent(LoginEvent.PhoneSignUp) })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoginTopViews(onEvent: (LoginEvent) -> Unit) {
    Column {
        Box(modifier = Modifier
            .align(Alignment.End)
            .clickable { onEvent(LoginEvent.PhoneSignUp) }
            .width(120.sdp)
            .height(76.sdp)) {
            Text(
                text = getStringWhenNeedPreview(id = R.string.sm_login_register_tips2),
                style = TextStyle(
                    color = Color.Black, fontSize = 36.stu, fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(
            modifier = Modifier
                .height(100.sdp)
                .fillMaxWidth()
        )

//        Image(
//            modifier = Modifier
//                .width(156.sdp)
//                .height(156.sdp)
//                .align(Alignment.CenterHorizontally)
//                .clip(RoundedCornerShape(42.sdp))
//                .combinedClickable(onLongClick = {
//                }, onClick = {}),
//            painter = BitmapUtil.adaptiveIconPainterResource(R.mipmap.cinmoore_icon),
//            contentDescription = getStringWhenNeedPreview(R.string.sm_login_app_name)
//        )

        //        Text(
        //            text = getStringWhenNeedPreview(id = R.string.sm_login_welcome2),
        //            style = textStyleGray28,
        //            textAlign = TextAlign.Center,
        //            modifier = Modifier
        //                .padding(top = 32.sdp)
        //                .fillMaxWidth()
        //        )

        VerticalLineSpacer(84)
    }

}

@Composable
fun BottomLinks(
    oneKeyLogin: MutableState<Boolean>,
    phoneOrEmailState: PhoneOrEmailState,
    showVerifyCodeLogin: Boolean,
    showPWDLoginLink: Boolean = true,
    onEvent: (LoginEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(top = 72.sdp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center

    ) {
        if (oneKeyLogin.value) {
            Row(modifier = Modifier
                .padding(20.sdp)
                .clickable { onEvent(LoginEvent.OnKeLogin) }) {
                Text(
                    text = getStringWhenNeedPreview(id = R.string.sm_login_bottom_link_one_key),
                    style = textStyleBlack28,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(end = 10.dp),
                )
            }
        }

        if (oneKeyLogin.value && showPWDLoginLink) {
            Divider(
                color = Color.LightGray, thickness = 1.dp, modifier = Modifier
                    .width(1.dp)
                    .height(28.sdp)
            )
        }

        if (showPWDLoginLink) {
            val text =
                if (showVerifyCodeLogin) getStringWhenNeedPreview(id = R.string.sm_login_bottom_link_password_login) else getStringWhenNeedPreview(
                    id = R.string.sm_login_bottom_verify_code_login
                )

            Row(modifier = Modifier
                .padding(20.sdp)
                .clickable{
                    onEvent(LoginEvent.GoPWDLogin(phoneOrEmailState.text))
                }) {
                Text(
                    text = text,
                    style = textStyleBlack28,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(start = 10.dp),
                )
            }
        }
    }
}


@Composable
private fun Logo(
    modifier: Modifier = Modifier,
) {
//    Image(
//        painter = painterResource(id = R.drawable.sm_icon),
//        modifier = modifier,
//        contentDescription = null
//    )
}

@Composable
private fun EmailAndButton(
    onEvent: (LoginEvent) -> Unit,
    onFocusChange: (Boolean) -> Unit,
) {

}

@Composable
fun PrivacyAgreementCheckBox(
    initCheckState: MutableState<Boolean>,
) {
    Spacer(modifier = Modifier.height(36.sdp))
    Row(
        modifier = Modifier
            .fillMaxWidth(1f),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        //        Checkbox(
        //            modifier = Modifier
        //                .height(39.sdp)
        //                .width(39.sdp),
        //            checked = initCheckState.value, onCheckedChange = {
        //                initCheckState.value = it
        //            })

        //        val color = MaterialTheme.colorScheme
        //        val imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Outlined.Circle
        //        val tint = if (selected) color.primary.copy(alpha = 0.8f) else color.white.copy(alpha = 0.8f)
        //        val background = if (selected) color.white else Color.Transparent

        //        IconButton(onClick = { },
        //            modifier = Modifier.offset(x = 4.dp, y = 4.dp),
        //        ) {
        //
        //            Icon(imageVector = ImageVector.vectorResource(R.drawable.sm_checkbox_unchecked_svg),
        //                contentDescription = "checkbox")
        //        }
        SMLoginCheckBox(checked = initCheckState.value) {
            initCheckState.value = !initCheckState.value
        }

        Text(
            text = getStringWhenNeedPreview(id = R.string.sm_login_notice_hint1),
            style = TextStyle(color = ColorGray, fontSize = 28.stu),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(start = 12.sdp)
                .wrapContentWidth()
        )

        Text(text = " ${getStringWhenNeedPreview(id = R.string.sm_login_notice_hint2)}",
            style = TextStyle(color = ColorPurple, fontSize = 28.stu),
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .wrapContentWidth()
                .clickable {
//                    ARouter
//                        .getInstance()
//                        .build("/webview/activity")
//                        .withString("uri", URLConstant.CONTRACT_SERVICE)
//                        .withBoolean("skipLogin", true)
//                        .navigation()
                })

        Text(
            text = " ${getStringWhenNeedPreview(id = R.string.sm_login_notice_hint3)} ",
            style = TextStyle(color = ColorGray, fontSize = 28.stu),
            textAlign = TextAlign.Start,
            modifier = Modifier.wrapContentWidth()
        )

        Text(text = getStringWhenNeedPreview(id = R.string.sm_login_notice_hint4),
            color = ColorPurple,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(color = ColorPurple, fontSize = 28.stu),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .wrapContentWidth()
                .clickable {
//                    ARouter
//                        .getInstance()
//                        .build("/webview/activity")
//                        .withString("uri", URLConstant.CONTRACT_PRIVACY)
//                        .withBoolean("skipLogin", true)
//                        .navigation()
                })
    }
}


@Preview
@Composable
fun VerifyCodeScreenPreview() {
    val initState = remember {
        mutableStateOf(true)
    }

    val loading = remember {
        mutableStateOf(false)
    }

    val vcLogin = remember {
        mutableStateOf(false)
    }
    LoginScreen(initState, loading, vcLogin.value, "andymao@qq.com") {}
}
