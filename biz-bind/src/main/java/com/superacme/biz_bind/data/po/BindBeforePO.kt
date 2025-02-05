package com.superacme.biz_bind.data.po

import com.google.gson.Gson
import com.superacme.biz_bind.data.ConfigLinkInfoVO


data class BindBeforePO(
    // 0-未绑定 1-已绑定 2-被自己绑定 3-设备不存在
    val checkCode: Int?,
    // checkCode=1 时 返回绑定用户的用户名（脱敏）
    val bindUser: String?,
    // 设备序列号
    val deviceName: String?,
    // 阿里云设备 云端唯一标识
    val iotId: String?,
    // 产品基础信息
    val productBaseInfo: ProductBaseInfoPO?,
    // 产品配置信息
    val productConfigInfo: List<ProductConfigInfoPO>?
)

data class ProductBaseInfoPO(
    // 产品key
    val productKey: String?,
    // 产品来源(0.自研,1.阿里云)
    val productSource: Int?,
    // 产品型号
    val model: String?,
    // 产品图片
    val productPicUrl: String?,
    // 通讯方式(1.wifi 4-移动通信（4G）)
    val netType: Int?
)

data class DeviceResetPO(
    val title: String?,
    val subTitle: String?,
    val descTitle: String?,
    val agreeTip: String?,
    val tipIconList: List<String>?,
    val tipList: List<String>?
)

data class ProductConfigInfoPO(
    // 配置项标识符
    val identifier: String?,
    // 配置项 值
    val value: String?
) {

    fun getLinkType(): MutableList<Int>? {
        return if (identifier != null && !value.isNullOrEmpty() && identifier == "linkType") {
            val array: Array<Int> = Gson().fromJson(value, Array<Int>::class.java)
            array.asList().toMutableList()
        } else {
            null
        }
    }

    fun getDefaultLinkType(): String? {
        return if (identifier != null && !value.isNullOrEmpty() && identifier == "defaultLinkType") {
            Gson().fromJson(value, String::class.java)
        } else {
            null
        }
    }

    fun getWifiHz(): MutableList<Int>? {
        return if (identifier != null && !value.isNullOrEmpty() && identifier == "wifiHz") {
            val array: Array<Int> = Gson().fromJson(value, Array<Int>::class.java)
            array.asList().toMutableList()
        } else {
            null
        }
    }

    fun getHotspotDirectConnection(): String? {
        return if (identifier != null && !value.isNullOrEmpty() && identifier == "hotspotDirectConnection") {
            Gson().fromJson(value, String::class.java)
        } else {
            null
        }
    }

    fun getScanRecharge(): String? {
        return if (identifier != null && !value.isNullOrEmpty() && identifier == "scanRecharge") {
            Gson().fromJson(value, String::class.java)
        } else {
            null
        }
    }

    fun getPowerType(): String? {
        return if (identifier != null && !value.isNullOrEmpty() && identifier == "powerType") {
            Gson().fromJson(value, String::class.java)
        } else {
            null
        }
    }

    fun getConfigLinkInfo(): ConfigLinkInfoVO? {
        return if (identifier != null && !value.isNullOrEmpty() && identifier == "configLinkInfo") {
            Gson().fromJson(value, ConfigLinkInfoVO::class.java)
//            val jb = JsonParser().parse(value).asJsonObject
//            val asJsonObject = jb.getAsJsonObject("deviceReset")
//            Gson().fromJson(asJsonObject, DeviceResetPO::class.java)
        } else {
            null
        }
    }
}





