package com.superacme.biz_bind

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superacme.biz_bind.core.isAcmeQrCode
import com.superacme.biz_bind.data.BindFlow
import com.superacme.biz_bind.data.enumerate.CheckCodeType
import com.superacme.biz_bind.intent.BindIntent
import com.superacme.biz_bind.repository.DeviceConnectRepository
import com.superacme.biz_bind.repository.impl.DeviceConnectRepoImpl
import com.superacme.biz_bind.state.BindFlowUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BindViewModel : ViewModel() {

    companion object {
        private const val TAG = "BindViewModel"
    }

    // 获取仓库实例
    private val repository: DeviceConnectRepository = DeviceConnectRepoImpl()

    // 页面级别的状态合集
    private val _pageState = MutableStateFlow(BindFlowUiState())
    val pageState: MutableStateFlow<BindFlowUiState> = _pageState

    fun processIntent(intent: BindIntent) {
        when (intent) {
            is BindIntent.UpdatePageState -> {
                pageState.update { state -> state.copy(pageScreen = intent.step) }
            }

            is BindIntent.UpdateQRValue -> {
                val qrValue = intent.qrValue
                Log.d(TAG, "qrValue: $qrValue")
                val result = isAcmeQrCode(qrValue)
                if (!result.first) {
                    R.string.bind_scan_device_qr_tip.toast()
                } else {
                    // 更新
                    pageState.update { state ->
                        state.copy(
                            productKey = result.second.first,
                            deviceName = result.second.second
                        )
                    }
                    processIntent(BindIntent.GetBindBeforeInfo)
                }
            }

            is BindIntent.UpdateWifiSsid -> {
                pageState.update { state -> state.copy(wifiSsid = intent.wifiSsid) }
            }

            is BindIntent.UpdateWifiPwd -> {
                pageState.update { state -> state.copy(wifiPwd = intent.wifiPwd) }
            }

            is BindIntent.GetBindBeforeInfo -> {
                viewModelScope.launch {
                    changeLoadingState(isLoading = true)
                    val deviceInfo = repository.getDeviceInfo(pageState.value.deviceName)
                    if (deviceInfo != null) {
                        // 更新信息
                        pageState.update { state -> state.copy(deviceInfo = deviceInfo) }

                        when (deviceInfo.checkCode) {
                            // 未绑定，被自己绑定，都走绑定流程
                            CheckCodeType.UNBIND, CheckCodeType.BIND_BY_SELF -> {
                                // 确定配网流程
                                processIntent(BindIntent.UpdatePageState(step = BindFlow.GuideReset))
                            }

                            CheckCodeType.BIND -> {
                                "被他人绑定".toast()
                                pageState.update { state -> state.copy(isFinish = true) }
                            }

                            CheckCodeType.DEVICE_NOT_EXIST -> {
                                "设备不存在".toast()
                                pageState.update { state -> state.copy(isFinish = true) }
                            }
                        }
                    } else {
                        "获取失败".toast()
                        pageState.update { state -> state.copy(isFinish = true) }
                    }
                    changeLoadingState(isLoading = false)
                }
            }

            is BindIntent.ConfigBindDevice -> {
                viewModelScope.launch {
                    changeLoadingState(isLoading = true)
                    val productKey = pageState.value.productKey
                    val deviceName = pageState.value.deviceName
                    val connectIdResult =
                        repository.configBindDevice(
                            productKey = productKey,
                            deviceName = deviceName
                        )
                    if (connectIdResult.isNotEmpty()) {
                        pageState.update { state -> state.copy(connectId = connectIdResult) }
                        processIntent(BindIntent.UpdatePageState(BindFlow.WifiChoose))
                    } else {
                        "配置失败".toast()
                    }
                    changeLoadingState(isLoading = false)
                }
            }

            is BindIntent.BindDevice -> {
                viewModelScope.launch {
                    changeLoadingState(isLoading = true)
                    val wifiSsid = pageState.value.wifiSsid
                    val wifiPwd = pageState.value.wifiPwd
                    val bindDeviceResult =
                        repository.bindDevice(wifiSSID = wifiSsid, wifiPW = wifiPwd)
                    if (bindDeviceResult) {
                        pageState.update { state -> state.copy(isFinish = true) }
                    } else {
                        "绑定失败".toast()
                    }
                    changeLoadingState(isLoading = false)
                }
            }
        }
    }

    private fun changeLoadingState(isLoading: Boolean) {
        pageState.update { state -> state.copy(isLoading = isLoading) }
    }

    override fun onCleared() {
        super.onCleared()
        repository.clearUp()
    }
}

private fun Int.toast() {

}

private fun String.toast() {

}
