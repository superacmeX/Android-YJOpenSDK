package com.superacme.login.view

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.acme.login.view.InnerLoginLogger
import java.util.regex.Pattern

// Consider an email valid if there's some text before and after a "@"
private const val EMAIL_VALIDATION_REGEX = "^(.+)@(.+)\$"
private const val PHONE_VALIDATION_REGEX =
    "^\\d{11}\$"

class PhoneOrEmailState :
    TextFieldState(validator = ::isEmailValid, errorFor = ::emailValidationError) {
    var justView: Boolean = false

    fun isEmail(): Boolean {
        return Pattern.matches(EMAIL_VALIDATION_REGEX, text)
    }
}

/**
 * Returns an error to be displayed or null if no error was found
 */
private fun emailValidationError(email: String): String {
    return "invalid email $email"
}

private fun isEmailValid(email: String): Boolean {
    //trim 一下，不然有空格或者换行的时候验证不过
    return Pattern.matches(EMAIL_VALIDATION_REGEX, email.trim()) || Pattern.matches(
        PHONE_VALIDATION_REGEX,
        email.trim()
    )
}

val POEStateSaver = phoneOrEmailStateSaver(PhoneOrEmailState())

fun phoneOrEmailStateSaver(state: PhoneOrEmailState): Saver<PhoneOrEmailState, Any> =
    listSaver<PhoneOrEmailState, Any>(
        save = {
            InnerLoginLogger.info(" PhoneForgetPWDScreen() called save ${it.text}")
            listOf(it.text, it.isFocusedDirty) },
        restore = {
            InnerLoginLogger.info(" PhoneForgetPWDScreen() called restore ${it[0]}")
            state.apply {
                text = it[0] as String
                isFocusedDirty = it[1] as Boolean
            }
        }
    )

