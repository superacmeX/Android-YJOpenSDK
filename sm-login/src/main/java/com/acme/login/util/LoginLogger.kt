package com.acme.login.util

import android.util.Log
import java.util.logging.Logger


class LoginLogger {
    companion object {
        private const val authorName = "andymao"

        @JvmStatic
        fun log(tag: String, msg: String) {
            Log.i(authorName, "LoginModule->$tag $msg")
        }

        @JvmStatic
        fun log(msg: String) {
            Log.i(authorName, "LoginModule->$msg")
        }

        @JvmStatic
        fun error(tag: String, msg: String) {
            Log.i(authorName, "LoginModule->$tag $msg")
        }
    }
}