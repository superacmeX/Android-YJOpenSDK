package com.acme.login.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.res.stringResource

val LocalPreviewDebug = compositionLocalOf {
    false
}

@Composable
fun getStringWhenNeedPreview(id: Int):String {
    return stringResource(id = id)

//    return if (debug) {
//        stringResource(id = strId)
//    } else {
//        GlobalProperties.application()
//            .getString(strId)
//    }
}

fun getStringWhenNeedPreview2(context: Context, id: Int):String {
    return context.getString(id)

//    return if (debug) {
//        stringResource(id = strId)
//    } else {
//        GlobalProperties.application()
//            .getString(strId)
//    }
}