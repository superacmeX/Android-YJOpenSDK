package com.superacme.login.view

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType.Companion.Sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acme.login.R
import com.acme.login.view.InnerLoginLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val Int.sdp: Dp get() = Dp(value = this.toFloat() / 2)
val Int.stu: TextUnit
    get() = TextUnit(
        this.toFloat() / 2,
        Sp
    )

internal val Color333 = Color(0xff333333)
internal val Color999 = Color(0xff999999)
internal val ColorF5 = Color(0xffffffff)
internal val ColorLightGray = Color(0x08000000)
internal val ColorGray = Color(0xff9B9DB1)
internal val ColorPurple = Color(0xff5F2AD1)
internal val ColorPurpleHalf = Color(0x105F2AD1)

//sm_login_bg_page_start
internal val PageGradientColorStart = Color(0xffE0D4F8)

//sm_login_bg_page_center
internal val PageGradientColorCenter = Color(0xffF0F4FF)

//sm_login_bg_page_end
internal val PageGradientColorEnd = Color(0x00ffffff)


val loginButtonRadius = 15.sdp
internal val loginButtonRadius_v2 = 50.sdp
internal val loginInputHeight = 102.sdp
//val loginButtonRadius = NORMAL_ROUNDED_RADIUS

val colorList = arrayListOf(PageGradientColorStart, PageGradientColorEnd)
val colorList2 = arrayListOf(Color.White, Color.White)
//val bgBrash = Brush.verticalGradient(colorList)
val bgBrash = Brush.verticalGradient(colorList2)

val textStyleGray32 = TextStyle(color = ColorGray, fontSize = 32.stu)
val pwdHint = TextStyle(color = ColorGray, fontSize = 32.stu)
val pwdNormal = TextStyle(color = Color333, fontSize = 32.stu)
val textStyleWhite32 = TextStyle(color = Color.White, fontSize = 32.stu)
val textStylePurple32 = TextStyle(color = ColorPurple, fontSize = 32.stu)
val textStyleBlack32 = TextStyle(color = Color.Black, fontSize = 32.stu)
val textStyleBlack28 = TextStyle(color = Color.Black, fontSize = 28.stu)
val textStyleGray28 = TextStyle(color = ColorGray, fontSize = 28.stu)
val textStyleBlack56 = TextStyle(color = Color.Black, fontSize = 56.stu)
val textStyleWhite34 = TextStyle(color = Color.White, fontSize = 34.stu)
val textStyleBlackBold48 =
    TextStyle(color = Color.Black, fontSize = 48.stu, fontWeight = FontWeight.Bold)

val buttonHeight54NoPadding = Modifier
    .fillMaxWidth()
    .height(102.sdp)
    .background(ColorPurple, shape = RoundedCornerShape(size = 30.dp))

val VerticalLineSpacer: Unit
    @Composable get() = Spacer(modifier = Modifier.height(16.dp))

private val trailIconModifier = Modifier
    .padding(end = 30.sdp)
    .size(40.sdp)

private val logTag = "andymao->loginCommonView"

@Composable
fun VerticalLineSpacer(height: Int = 16) {
    Spacer(modifier = Modifier.height(height.sdp))
}

@Composable
fun BackButton(onClick: () -> Unit) {
    Box(modifier = Modifier
        .width(90.sdp)
        .height(90.sdp)
        .clickable {
            onClick()
        }) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowLeft,
                contentDescription = "Back"
            )
        }
    }
}

@Composable
fun LoginButton(
    modifier: Modifier = Modifier,
    enable: Boolean,
    text: String,
    style: TextStyle = textStyleWhite34,
    onClick: () -> Unit = {}
) {
    val alpha = if (!enable) 0.5f else 1f
    Box(
        modifier = modifier
            .alpha(alpha)
    ) {
        DeferClickButton(
            onClick = onClick,
            enabled = enable,
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
                disabledContentColor = Color.White
            ),

            // 如果这里的color不同。可以看出有两个区域
//            shape = RoundedCornerShape(CornerSize(30.dp)),
//            modifier = Modifier.background(
//                color = Color.Black,
//                shape = RoundedCornerShape(CornerSize(30.dp))),

            shape = RoundedCornerShape(CornerSize(loginButtonRadius_v2)),
            modifier = buttonHeight54NoPadding.semantics { contentDescription = "login button" },
        ) {
            Text(
                style = style,
                text = text,
            )
        }
    }
}

@Composable
fun PwdInputText(
    modifier: Modifier? = null,
    pwdState: MutableState<String> = remember { mutableStateOf("") },
    inputMaxLength: Int? = null,
    hintStrId: Int = R.string.sm_login_password_hint,
    focusChange: ((Boolean) -> Unit) = {},
    onValueChanged: ((String) -> Unit) = {},
) {
    val (passwordIsShow, onPasswordIsShowChange) = rememberSaveable {
        mutableStateOf(false)
    }
    val maxLength = inputMaxLength ?: Int.MAX_VALUE
    InnerLoginLogger.info("$logTag PwdInputText() called with: maxLength= $maxLength, pwdState.value=${pwdState.value}")
    val realModifier = modifier?.height(loginInputHeight) ?: Modifier.height(loginInputHeight)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var lastToastTime by remember {
        mutableLongStateOf(-1)
    }


    BgBasicTextField(
        value = pwdState.value,
        onValueChange = {
            val trimToMaxLength = if (inputMaxLength != null && it.length > inputMaxLength) {
                if (System.currentTimeMillis() - lastToastTime > 1000) {
                    lastToastTime = System.currentTimeMillis()
                    coroutineScope.launch(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            R.string.sm_login_pwd_length_greater_than_16,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                it.substring(0, inputMaxLength)
            } else {
                it
            }
            InnerLoginLogger.info("$logTag PwdInputText() called onValueChange trimToMaxLength.length=${trimToMaxLength.length}")
            pwdState.value = trimToMaxLength
            onValueChanged(trimToMaxLength)
        },
        maxLength = maxLength,
        hintText = {
            Text(
                text = stringResource(id = hintStrId),
                style = pwdHint,
            )
        },
        backgroundModifier = Modifier.background(
            ColorLightGray,
            RoundedCornerShape(loginButtonRadius_v2)
        ),
        horizontalPadding = 40.sdp,
        modifier = realModifier
            .onFocusChanged { focusState -> focusChange(focusState.isFocused) }
            .semantics { contentDescription = "input password" },

        textStyle = pwdNormal,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Email
        ),
        keyboardActions = KeyboardActions(
            onDone = {
            }
        ),
        visualTransformation = if (!passwordIsShow) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },

        trailing = {
            Image(
                painter = painterResource(id = if (passwordIsShow) R.drawable.sm_login_btn_pwd_open else R.drawable.sm_login_btn_pwd_close),
                contentDescription = "",
                modifier = trailIconModifier
                    .clickable {
                        onPasswordIsShowChange(!passwordIsShow)
                    })
        }

    )
}

@OptIn(ExperimentalComposeUiApi::class) // for software keyboard
@Composable
fun PhoneOrEmail(
    showKeyBord: Boolean = false,
    onValueChange: (String) -> Unit = {},
    onClearButtonClick: () -> Unit = {},
    phoneOrEmailState: PhoneOrEmailState = remember { PhoneOrEmailState() },
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {}
) {
    val focusReq = remember { FocusRequester() }
    val modifier = Modifier
        .fillMaxWidth()
        .height(loginInputHeight)
        .focusRequester(focusReq)
        .onFocusChanged { focusState ->
            phoneOrEmailState.onFocusChange(focusState.isFocused)
            if (!focusState.isFocused) {
                phoneOrEmailState.enableShowErrors()
            }
        }

    InnerLoginLogger.info("$logTag PhoneOrEmail() called in compose text=${phoneOrEmailState.text}, phoneOrEmailState=${phoneOrEmailState.hashCode()}")

    BgBasicTextField(
        value = phoneOrEmailState.text,
        onValueChange = {
            InnerLoginLogger.info("$logTag PhoneOrEmail() called onValueChange=$it")
            phoneOrEmailState.text = it
            onValueChange(it)
        },
        hintText = {
            Text(
                text = stringResource(id = R.string.sm_login_reg_text_hint),
                style = pwdHint,
            )
        },
        backgroundModifier = Modifier.background(
            ColorLightGray,
            RoundedCornerShape(loginButtonRadius_v2)
        ),
        maxLength = 100,
        modifier = modifier.semantics { contentDescription = "input account" },
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = imeAction,
            keyboardType = KeyboardType.Email
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onImeAction()
            }
        ),
        trailing = {
            val showClearBtn = phoneOrEmailState.text.isNotEmpty()
            InnerLoginLogger.info("$logTag PhoneOrEmail() called trailing=$showClearBtn")
            if (showClearBtn) {
                Icons.Default
                Image(
                    painter = painterResource(id = R.drawable.sm_login_btn_input_close),
                    contentDescription = "",
                    modifier = trailIconModifier
                        .clickable {
                            InnerLoginLogger.info("$logTag PhoneOrEmail() called showClearBtn clear text=${phoneOrEmailState.text}, phoneOrEmailState=${phoneOrEmailState.hashCode()}")
                            phoneOrEmailState.text = ""
                            onClearButtonClick()
                        })
            }

        },

        )
    if (showKeyBord) {
        LaunchedEffect(Unit) {
            focusReq.requestFocus()
        }
    }
}

@Composable
fun BgBasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    fontSize: TextUnit = 16.sp,
    fontColor: Color = Color333,
    maxLength: Int? = null,
    contentAlignment: Alignment.Vertical = Alignment.CenterVertically,
    leading: (@Composable RowScope.() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    horizontalPadding: Dp = 16.dp,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    cursorBrush: Brush = SolidColor(MaterialTheme.colorScheme.primary),
    backgroundModifier: Modifier = Modifier,
    hintText: (@Composable () -> Unit)? = null,
) {
    //只设置一次
    var lastSelectPosition by remember {
        mutableStateOf(TextRange(value.length))
    }

    //value变化的时候，重新设置textFieldValue。例如外面清空了输入框，此时textFieldValue的text还是之前的值，导致光标位置不对
    var textFieldValue by remember(value) {
        InnerLoginLogger.info("$logTag BgBasicTextField() called in remember $value, lastSelectPosition=$lastSelectPosition")
        mutableStateOf(TextFieldValue(text = value, lastSelectPosition))
    }

    BasicTextField(
        value = textFieldValue,
        onValueChange = { originalField ->
            val maxLengthLocal = maxLength ?: Int.MAX_VALUE
            val inputStr = originalField.text
            val trimedText = if (inputStr.length > maxLengthLocal) {
                //使value不超过maxLength
                InnerLoginLogger.info("BgBasicTextField handle too long")
                inputStr.substring(0, maxLengthLocal)
            }
//            else if (maxLines == 1 && inputStr.contains('\n')) {
//                //处理特殊情况下单行输入框能输入换行符
//                Logger.info("BgBasicTextField handle \\ n")
//                inputStr.replace("\n", "")
//            }
            else {
                inputStr
            }
            textFieldValue = originalField.copy(text = trimedText)

            lastSelectPosition = textFieldValue.selection

            InnerLoginLogger.info("BgBasicTextField originalField=${originalField.text}, trimedText=$trimedText, maxLength=$maxLengthLocal")
            onValueChange(originalField.text)
        },
        textStyle = textStyle.copy(color = fontColor, fontSize = fontSize),
        singleLine = maxLines == 1,
        maxLines = maxLines,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        onTextLayout = onTextLayout,
        interactionSource = interactionSource,
        cursorBrush = cursorBrush,
        decorationBox = {
            Row(
                backgroundModifier
//                    .height(IntrinsicSize.Min) //这个会导致点击的时候 selection 有问题
                    .padding(start = horizontalPadding)
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leading != null) {
                    leading()
                    Spacer(Modifier.width(horizontalPadding))
                }

                Box(
                    Modifier
                        .weight(1f)
                        .align(contentAlignment)
                        .let {
                            if (contentAlignment != Alignment.CenterVertically)
                                it.padding(vertical = horizontalPadding / 2)
                            else
                                it
                        },
                ) {
                    if (value.isEmpty() && hintText != null) {
                        Box(
                            Modifier.align(Alignment.CenterStart)
                        ) {
                            hintText.invoke()
                        }
                    }
                    it()
                }

                if (trailing != null) {
                    Spacer(Modifier.width(horizontalPadding))
                    trailing()
                }
            }
        }
    )
}

@Preview
@Composable
fun PreviewShowTextInput() {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()

    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
        ) {
            Box(modifier = Modifier.padding(start = 39.sdp, top = 87.sdp)) {
                BackButton { }
            }

            Box(
                modifier = Modifier
                    .padding(top = 33.sdp)
            ) {
                PhoneOrEmail()
            }

            Box(
                modifier = Modifier
                    .padding(top = 33.sdp)
            ) {
                PhoneOrEmail()
            }

            Box(
                modifier = Modifier
                    .padding(top = 33.sdp)
            ) {
                PhoneOrEmail(phoneOrEmailState = PhoneOrEmailState().also {
                    it.text = "andymao@qq.com"
                })
            }

//            Box(
//                modifier = Modifier
//                    .padding(top = 33.sdp)
//            ) {
//                ProcessLoading()
//            }
            Box(
                modifier = Modifier
                    .padding(top = 33.sdp)
            ) {
                PwdInputText(inputMaxLength = 13)
            }

            Box(
                modifier = Modifier
                    .padding(top = 33.sdp)
            ) {
                PwdInputText()
            }

            LoginButton(
                modifier = Modifier.padding(top = 30.sdp),
                enable = true,
                text = "Go Next1"
            )

            LoginButton(
                modifier = Modifier.padding(top = 30.sdp),
                enable = false,
                text = "Go Next1"
            )

        }

    }
}

@Composable
fun DeferClickButton(
    onClick: () -> Unit,
    delayTimeMilliseconds: Int = 1000,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {

    var clicked by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = clicked, block = {
        delay(delayTimeMilliseconds.toLong())
        clicked = false
    })


    Button(
        onClick = {
            if (!clicked) {
                clicked = true
                onClick()
            } else {
            }
        },
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        elevation = elevation,
        shape = shape,
        border = border,
        colors = colors,
        contentPadding = contentPadding,
        content = content

    )
}