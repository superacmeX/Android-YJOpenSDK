package com.superacme.biz_bind.repository.impl

import com.google.gson.Gson
import com.superacme.biz_bind.data.DeviceInfoVO
import com.superacme.biz_bind.data.ProductConfigInfoVO
import com.superacme.biz_bind.data.ProductInfoVO
import com.superacme.biz_bind.data.enumerate.CheckCodeType
import com.superacme.biz_bind.data.enumerate.HotspotDirectConnectionType
import com.superacme.biz_bind.data.enumerate.LinkType
import com.superacme.biz_bind.data.enumerate.NetType
import com.superacme.biz_bind.data.enumerate.PowerType
import com.superacme.biz_bind.data.enumerate.ProductSourceType
import com.superacme.biz_bind.data.enumerate.ScanRechargeType
import com.superacme.biz_bind.data.enumerate.WifiHzType
import com.superacme.biz_bind.data.po.BindBeforePO
import com.superacme.biz_bind.repository.DeviceConnectRepository
import com.superacme.common.logan.Logger
import com.superacme.sdk_device_connect_ap.api.ApConnectApi
import com.superacme.sdk_device_connect_ap.impl.ApConnectApiImpl
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class DeviceConnectRepoImpl : DeviceConnectRepository {

    // SDK接口
    private val connectApi: ApConnectApi = ApConnectApiImpl()

    companion object {
        private const val TAG = "DeviceConnectRepoImpl"
    }

    override suspend fun getDeviceInfo(deviceName: String): DeviceInfoVO? {
        return suspendCancellableCoroutine {
            connectApi.getPrepareDeviceInfo(deviceName = deviceName, callBack = { info, error ->
                Logger.i(TAG, "getBindBeforeInfo: info : $info")
                Logger.i(TAG, "getBindBeforeInfo: error : $error")
                if (info != null) {
                    val bindBeforePO = Gson().fromJson(info, BindBeforePO::class.java)
                    Logger.i(TAG, "bindBeforePO: info : $bindBeforePO")
                    it.resume(parseDeviceInfo(bindBeforePO))
                } else {
                    it.resume(null)
                }
            })
        }
    }

    override suspend fun configBindDevice(productKey: String, deviceName: String): String {
        return suspendCancellableCoroutine {
            connectApi.configBindDevice(
                productKey = productKey,
                deviceName = deviceName,
                callBack = { info, error ->
                    it.resume(connectApi.deviceHotspotName() ?: "")
                })
        }
    }

    override suspend fun bindDevice(wifiSSID: String, wifiPW: String): Boolean {
        return suspendCancellableCoroutine {
            connectApi.bindDevice(wifiSSID = wifiSSID, wifiPW = wifiPW, bindStep = { step ->
                Logger.i(TAG, "DeviceConnectRepoImpl.bindDevice: step : $step")
            }, result = { failStep, result, error ->
                Logger.i(TAG, "DeviceConnectRepoImpl.bindDevice: failStep : $failStep")
                Logger.i(TAG, "DeviceConnectRepoImpl.bindDevice: result : $result")
                Logger.i(TAG, "DeviceConnectRepoImpl.bindDevice: error : $error")
                if (result != null) {
                    it.resume(true)
                }
                if (error != null) {
                    it.resume(false)
                }
            })
        }
    }

    override fun clearUp() {
        connectApi.clearUp()
    }

    private fun parseDeviceInfo(bindBeforePO: BindBeforePO): DeviceInfoVO {
        val deviceInfoVO = DeviceInfoVO()
        deviceInfoVO.deviceName = bindBeforePO.deviceName ?: ""
        deviceInfoVO.checkCode = bindBeforePO.checkCode?.let {
            when (it) {
                CheckCodeType.UNBIND.type -> CheckCodeType.UNBIND
                CheckCodeType.BIND.type -> CheckCodeType.BIND
                CheckCodeType.BIND_BY_SELF.type -> CheckCodeType.BIND_BY_SELF
                CheckCodeType.DEVICE_NOT_EXIST.type -> CheckCodeType.DEVICE_NOT_EXIST
                else -> CheckCodeType.UNBIND
            }
        } ?: CheckCodeType.UNBIND
        deviceInfoVO.bindUser = bindBeforePO.bindUser ?: ""

        val productInfoVO = ProductInfoVO()
        productInfoVO.productKey = bindBeforePO.productBaseInfo?.productKey ?: ""
        productInfoVO.productSource = bindBeforePO.productBaseInfo?.productSource?.let {
            when (it) {
                ProductSourceType.SELF_CLOUD.type -> ProductSourceType.SELF_CLOUD
                ProductSourceType.ALI_CLOUD.type -> ProductSourceType.ALI_CLOUD
                else -> ProductSourceType.SELF_CLOUD
            }
        } ?: ProductSourceType.SELF_CLOUD
        productInfoVO.model = bindBeforePO.productBaseInfo?.model ?: ""
        productInfoVO.productPicUrl = bindBeforePO.productBaseInfo?.productPicUrl ?: ""
        productInfoVO.netType = bindBeforePO.productBaseInfo?.netType?.let {
            when (it) {
                NetType.WIFI.type -> NetType.WIFI
                NetType.FOUR_G.type -> NetType.FOUR_G
                else -> NetType.WIFI
            }
        } ?: NetType.WIFI
        deviceInfoVO.productInfoVO = productInfoVO

        val productConfigInfoVO = ProductConfigInfoVO()
        bindBeforePO.productConfigInfo?.forEach { infoPo ->

            val linkTypeOrigin = infoPo.getLinkType()
            if (linkTypeOrigin != null) {
                productConfigInfoVO.linkType = linkTypeOrigin.map {
                    when (it) {
                        LinkType.WIFI_AP_HOT.type -> LinkType.WIFI_AP_HOT
                        LinkType.BLUETOOTH.type -> LinkType.BLUETOOTH
                        LinkType.CAMERA_SCAN.type -> LinkType.CAMERA_SCAN
                        LinkType.FOUR_G.type -> LinkType.FOUR_G
                        else -> LinkType.WIFI_AP_HOT
                    }
                }.toSet().toMutableList()
            }

            val defaultLinkTypeOrigin = infoPo.getDefaultLinkType()
            if (defaultLinkTypeOrigin != null) {
                productConfigInfoVO.defaultLinkType = when (defaultLinkTypeOrigin) {
                    LinkType.WIFI_AP_HOT.type.toString() -> LinkType.WIFI_AP_HOT
                    LinkType.BLUETOOTH.type.toString() -> LinkType.BLUETOOTH
                    LinkType.CAMERA_SCAN.type.toString() -> LinkType.CAMERA_SCAN
                    LinkType.FOUR_G.type.toString() -> LinkType.FOUR_G
                    else -> LinkType.WIFI_AP_HOT
                }
            }

            val getWifiHzOrigin = infoPo.getWifiHz()
            if (getWifiHzOrigin != null) {
                productConfigInfoVO.wifiHz = getWifiHzOrigin.map {
                    when (it) {
                        WifiHzType.WIFI_HZ_2.type -> WifiHzType.WIFI_HZ_2
                        WifiHzType.WIFI_HZ_5.type -> WifiHzType.WIFI_HZ_5
                        else -> WifiHzType.WIFI_HZ_5
                    }
                }.toSet().toMutableList()
            }

            val hotspotDirectConnectionOrigin = infoPo.getHotspotDirectConnection()
            if (hotspotDirectConnectionOrigin != null) {
                productConfigInfoVO.hotspotDirectConnection = when (hotspotDirectConnectionOrigin) {
                    HotspotDirectConnectionType.NOT_SUPPORT.type.toString() -> HotspotDirectConnectionType.NOT_SUPPORT
                    HotspotDirectConnectionType.SUPPORT.type.toString() -> HotspotDirectConnectionType.SUPPORT
                    else -> HotspotDirectConnectionType.NOT_SUPPORT
                }
            }

            val scanRechargeOrigin = infoPo.getScanRecharge()
            if (scanRechargeOrigin != null) {
                productConfigInfoVO.scanRecharge = when (scanRechargeOrigin) {
                    ScanRechargeType.NOT_SUPPORT.type.toString() -> ScanRechargeType.NOT_SUPPORT
                    ScanRechargeType.SUPPORT.type.toString() -> ScanRechargeType.SUPPORT
                    else -> ScanRechargeType.SUPPORT
                }
            }

            val powerTypeOrigin = infoPo.getPowerType()
            if (powerTypeOrigin != null) {
                productConfigInfoVO.powerType = when (powerTypeOrigin) {
                    PowerType.BATTERY_DEVICE.type.toString() -> PowerType.BATTERY_DEVICE
                    PowerType.ELECTRICITY_DEVICE.type.toString() -> PowerType.ELECTRICITY_DEVICE
                    else -> PowerType.ELECTRICITY_DEVICE
                }
            }

            val configLinkInfoOrigin = infoPo.getConfigLinkInfo()
            if (configLinkInfoOrigin != null) {
                productConfigInfoVO.configLinkInfo = configLinkInfoOrigin
            }
        }

        deviceInfoVO.productConfigInfoVO = productConfigInfoVO
        Logger.i(TAG, "parse deviceInfoVO: $deviceInfoVO")
        return deviceInfoVO
    }
}