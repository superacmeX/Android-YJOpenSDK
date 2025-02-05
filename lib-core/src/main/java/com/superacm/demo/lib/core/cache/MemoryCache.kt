package com.superacm.demo.lib.core.cache

import com.superacm.demo.lib.core.model.Device

object MemoryCache {

    private val deviceCache = mutableMapOf<String, Device>()

    fun getDevice(deviceId: String) = deviceCache[deviceId]

    fun saveDevice(deviceId: String, device: Device) {
        deviceCache[deviceId] = device
    }

}