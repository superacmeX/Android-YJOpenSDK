package com.acme.opensdk.demo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.acme.login.LoginActivity
import com.acme.common.account.login.YJLoginBusiness


import com.superacm.demo.home.HomeActivity

class StarterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (YJLoginBusiness.isLogin()) {//首页
            startActivity(Intent(this, HomeActivity::class.java))
        } else { //跳登录页面
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}

