package com.superacme.biz_bind.repository

import com.superacme.biz_bind.data.DeviceInfoVO

interface DeviceConnectRepository {
    suspend fun getDeviceInfo(deviceName: String): DeviceInfoVO?

    suspend fun configBindDevice(productKey: String, deviceName: String): String

    suspend fun bindDevice(wifiSSID: String, wifiPW: String): Boolean

    fun clearUp()
}