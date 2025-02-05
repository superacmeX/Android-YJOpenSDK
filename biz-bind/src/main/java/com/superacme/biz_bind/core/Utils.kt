package com.superacme.biz_bind.core

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity

fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

fun isAcmeQrCode(qrValue: String): Pair<Boolean, Pair<String, String>> {
    if (qrValue.startsWith("https://superacme")) {
        // 第一种
        val uri = Uri.parse(qrValue)
        val pk = uri.getQueryParameter("pk")
        val dn = uri.getQueryParameter("dn")
        return if (pk?.isNotEmpty() == true && dn?.isNotEmpty() == true) {
            Pair(true, Pair(pk, dn))
        } else {
            Pair(false, Pair("", ""))
        }
    } else {
        val split = qrValue.split("\n")
        return if (split.size == 3 || split.size == 4) {
            // 第二种
            val code = split[0]
            val pk = split[1]
            val dn = split[2]
            if (code == "cm") {
                Pair(true, Pair(pk, dn))
            } else {
                Pair(false, Pair("", ""))
            }
        } else {
            Pair(false, Pair("", ""))
        }
    }
}
