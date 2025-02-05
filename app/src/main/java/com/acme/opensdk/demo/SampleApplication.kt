package com.acme.opensdk.demo

import android.app.Application
import android.content.Intent
import android.util.Log
import com.acme.common.account.session.RefreshTokenInvalidListener
import com.acme.common.account.session.YJSessionManager
import com.acme.login.LoginActivity
import com.microbit.RMPConfigEnv
import com.microbit.RMPConfigRegion
import com.microbit.rmplayer.RMPConfig
import com.superacm.demo.home.video.util.ImageLoadUtil
import com.superacm.opensdk.base.Configuration
import com.superacm.opensdk.base.GatewayConfig
import com.superacme.common.logan.strategy.LogStrategy
import com.superacme.opensdk.YJOpenSdk

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ImageLoadUtil.init(this)
        val config = Configuration.Builder(this)
            .gatewayConfig(
                gatewayConfig = GatewayConfig.Builder()
                    .baseUrl("https://api-cn-test.superacme.com")
                    .appKey("bjrD9M0V")
                    .appSecret("2fa800a707ffe91d98cc7b9812daa8b7")
                    .header("appKey", "bjrD9M0V")
                    .header("appVersion", "0.0.1")
                    .header("clientType", "android")
                    .header("utdid", "849574695")
                    .header("utdName", "2201123C")
                    .header("timeZone", "8")
                    .header("Accept-Language", "zh-CN")
                    .build()
            )
            .logStrategy(object : LogStrategy {
                override fun log(priority: Int, tag: String, message: String) {
                    Log.println(priority, "Demo-$tag", message)
                }
            })
            .region("China")
            .debugMode(true)
            // todo 参数传递先用 map
            .params("timeZone", 8)
            .params("countryCode", "CN")
            .params("Accept-Language", "zh-CN")
            .build()

        YJSessionManager.setRefreshTokenInvalidListener(object : RefreshTokenInvalidListener {
            override fun onRefreshTokenInvalided() { //需要跳转到登录界面，可能会返回多次，需要应用开发者对这种情况下进行处理
                startActivity(Intent(this@SampleApplication, LoginActivity::class.java).apply {
                    this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })

            }
        })

        YJOpenSdk.init(config)
        RMPConfig.setBootConfig(this, RMPConfigRegion.RMP_REGION_CN, RMPConfigEnv.RMP_ENV_TEST);
    }

}