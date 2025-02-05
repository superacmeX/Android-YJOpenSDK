package com.acme.login.vm

import android.content.Context

interface IUMLogin {
    fun init(context: Context)
    fun checkOnKeyLoginEnable(): Boolean
    fun quitLoginPage()
    fun hideLoginLoading()
    fun destroy()
}