package com.acme.opensdk.demo

import android.app.Application
import android.content.Intent
import android.util.Log
import com.acme.common.account.logger.Logger
import com.acme.common.account.session.RefreshTokenInvalidListener
import com.acme.common.account.session.YJSessionManager
import com.acme.login.LoginActivity
import com.microbit.RMPConfigEnv
import com.microbit.RMPConfigRegion
import com.microbit.rmplayer.RMPConfig
import com.superacm.demo.home.HomeApi
import com.superacm.demo.home.video.util.ImageLoadUtil
import com.superacm.opensdk.base.Configuration
import com.superacm.opensdk.base.GatewayConfig
import com.superacm.opensdk.base.LANGUAGE
import com.superacm.opensdk.base.OpenSdkInternal
import com.superacm.opensdk.base.REGION
import com.superacme.common.logan.strategy.LogStrategy
import com.superacme.common_network.YJGateWay
import com.superacme.common_network.call.ApiCall
import com.superacme.opensdk.YJOpenSdk
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ImageLoadUtil.init(this)
        val config = Configuration.Builder(this)
            .gatewayConfig(
                gatewayConfig = GatewayConfig.Builder()
                    .acceptLanguage(LANGUAGE.ZH_CN)
                    .build()
            )
            .appKey("xxxxx")
            .appSecret("xxxxxxxxx")
            .region(REGION.CHINA)
            .debugMode(true)
            .testEnv(true)
            .logStrategy(object : LogStrategy {
                override fun log(priority: Int, tag: String, message: String) {
                    Log.println(priority, tag, message)
                }
            })
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

        authLoginTest()


    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun authLoginTest() {
        GlobalScope.launch {
            YJOpenSdk.authLogin("cn","c38yH3nv3Rx9Atn4") {result,_->
                getDeviceList()
            }
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun getDeviceList() {
        GlobalScope.launch {
                ApiCall.apiCall(apiCall={YJGateWay.createApi(HomeApi::class.java).getDeviceList()},
                    onSuccess={
                        Logger.debug("YJ",it.data.toString())
                    },
                    onError={
                        Log.i("YJ",it.toString())
                    })

        }
    }

}