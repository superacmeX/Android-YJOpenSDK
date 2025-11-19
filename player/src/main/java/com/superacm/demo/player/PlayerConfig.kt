package com.superacm.demo.player

import android.content.Context
import android.util.Log
import com.microbit.RendererCommon
import com.microbit.rmplayer.RMPDecoderStrategy

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
    // AP mode configurations
    val apIp: String = "192.168.43.1",
    val apPort: String = "6684",
    val localIp: String = "0.0.0.0",
    val clientId: String = "demo_client",
    val vdecodeStrategy: Int = RMPDecoderStrategy.HARDWARE_FIRST
) {
    companion object {
        @JvmStatic
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

        @JvmStatic
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
            apIp: String = "192.168.43.1",
            apPort: String = "6684",
            localIp: String = "0.0.0.0",
            clientId: String = "demo_client"
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
            apIp: String = "192.168.43.1",
            apPort: String = "6684",
            localIp: String = "0.0.0.0",
            clientId: String = "demo_client"
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