package com.superacme.biz_bind.intent

import com.superacme.biz_bind.data.BindFlow

sealed class BindIntent {

    /**
     * 更新页面状态
     */
    data class UpdatePageState(val step: BindFlow) : BindIntent()

    /**
     * 更新二维码ø值
     */
    data class UpdateQRValue(val qrValue: String) : BindIntent()

    /**
     * 更新Wi-Fi SSID
     */
    data class UpdateWifiSsid(val wifiSsid: String) : BindIntent()

    /**
     * 更新Wi-Fi PWD
     */
    data class UpdateWifiPwd(val wifiPwd: String) : BindIntent()

    /**
     * 获取绑定前设备信息
     */
    data object GetBindBeforeInfo : BindIntent()

    /**
     * 配置设备绑定信息
     */
    data object ConfigBindDevice : BindIntent()

    /**
     * 执行绑定设备
     */
    data object BindDevice : BindIntent()
}