package com.superacme.biz_bind.state

import com.superacme.biz_bind.data.BindFlow
import com.superacme.biz_bind.data.DeviceInfoVO


/**
 * 密封类，存储容器页面状态
 */
data class BindFlowUiState(
    val pageScreen: BindFlow = BindFlow.Default,
    // 国内 cm a3gZ7505Z6ud 550001000028000000157
    val deviceName: String = "",
    val productKey: String = "",
    val wifiSsid: String = "SuperACME_Product_Test",
    val wifiPwd: String = "SUpro@com18",
    val deviceInfo: DeviceInfoVO = DeviceInfoVO(),
    val connectId: String = "",
    val isFinish: Boolean = false,
    val isLoading: Boolean = false
)