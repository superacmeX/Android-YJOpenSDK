package com.superacm.demo.player

import android.content.Context
import android.util.Log
import com.microbit.RendererCommon

data class PlayerConfig(
    val context: Context,
    val deviceName: String,
    val productKey: String,
    val deviceId: String? = null,
    val channelId: String? = null,
    val quality: Int = 0,
    val enableAudio: Boolean = true,
    val enableVideo: Boolean = true,
    val timeout: Int = 30000,
    val scalingType: RendererCommon.ScalingType = RendererCommon.ScalingType.SCALE_ASPECT_FIT,
    val enableHardwareScaler: Boolean = false,
    val enableLogging: Boolean = true,
    val logLevel: Int = Log.INFO,
    // AP mode specific fields
    val apIp: String? = null,
    val apPort: String? = null,
    val localIp: String? = null,
    val clientId: String? = null,
    val vdecodeStrategy: Int = 0
) {
    companion object {
        fun createLiveConfig(
            context: Context,
            deviceName: String,
            productKey: String,
            deviceId: String? = null
        ): PlayerConfig {
            return PlayerConfig(
                context = context,
                deviceName = deviceName,
                productKey = productKey,
                deviceId = deviceId,
                enableAudio = true,
                enableVideo = true
            )
        }

        fun createVodConfig(
            context: Context,
            deviceName: String,
            productKey: String,
            deviceId: String? = null,
            startSec: Long = 0,
            endSec: Long = 0
        ): PlayerConfig {
            return PlayerConfig(
                context = context,
                deviceName = deviceName,
                productKey = productKey,
                deviceId = deviceId,
                enableAudio = true,
                enableVideo = true
            )
        }

        @JvmStatic
        fun createApLiveConfig(
            context: Context,
            apIp: String,
            apPort: String,
            localIp: String,
            clientId: String
        ): PlayerConfig {
            return PlayerConfig(
                context = context,
                deviceName = "",
                productKey = "",
                apIp = apIp,
                apPort = apPort,
                localIp = localIp,
                clientId = clientId,
                enableAudio = true,
                enableVideo = true
            )
        }

        @JvmStatic
        fun createApVodConfig(
            context: Context,
            apIp: String,
            apPort: String,
            localIp: String,
            clientId: String
        ): PlayerConfig {
            return PlayerConfig(
                context = context,
                deviceName = "",
                productKey = "",
                apIp = apIp,
                apPort = apPort,
                localIp = localIp,
                clientId = clientId,
                enableAudio = true,
                enableVideo = true
            )
        }
    }
    
    fun toPlayerParam(): PlayerParam {
        val param = PlayerParam(deviceName, productKey, deviceId)
        param.deviceId = deviceId
        param.channelId = channelId
        param.quality = quality
        param.enableAudio = enableAudio
        param.enableVideo = enableVideo
        param.timeout = timeout
        return param
    }
} 