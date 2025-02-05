package com.superacme.biz_bind.data

import androidx.annotation.Keep
import com.superacme.biz_bind.data.enumerate.CheckCodeType
import com.superacme.biz_bind.data.enumerate.HotspotDirectConnectionType
import com.superacme.biz_bind.data.enumerate.LinkType
import com.superacme.biz_bind.data.enumerate.NetType
import com.superacme.biz_bind.data.enumerate.PowerType
import com.superacme.biz_bind.data.enumerate.ProductSourceType
import com.superacme.biz_bind.data.enumerate.ScanRechargeType
import com.superacme.biz_bind.data.enumerate.WifiHzType

@Keep
data class DeviceInfoVO(
    // 设备序列号
    var deviceName: String = "",
    // 绑定状态
    var checkCode: CheckCodeType = CheckCodeType.UNBIND,
    // 已经绑定状态下，返回绑定用户的用户名（脱敏）
    var bindUser: String = "",
    // 设备对应的产品信息
    var productInfoVO: ProductInfoVO = ProductInfoVO(),
    // 设备对应的产品配置信息
    var productConfigInfoVO: ProductConfigInfoVO = ProductConfigInfoVO()
)

@Keep
data class ProductInfoVO(
    // 产品key
    var productKey: String = "",
    // 产品来源
    var productSource: ProductSourceType = ProductSourceType.SELF_CLOUD,
    // 产品型号
    var model: String = "",
    // 产品图片
    var productPicUrl: String = "",
    // 通讯方式
    var netType: NetType = NetType.WIFI
)

@Keep
data class ProductConfigInfoVO(
    // 配网方式
    var linkType: MutableList<LinkType> = mutableListOf(),
    // 默认配网方式
    var defaultLinkType: LinkType = LinkType.WIFI_AP_HOT,
    // wifi类产品 wifi频率
    var wifiHz: MutableList<WifiHzType> = mutableListOf(),
    // 是否支持热点直连
    var hotspotDirectConnection: HotspotDirectConnectionType = HotspotDirectConnectionType.NOT_SUPPORT,
    // 是否支持扫码充值流量
    var scanRecharge: ScanRechargeType = ScanRechargeType.SUPPORT,
    // 设备电源类型
    var powerType: PowerType = PowerType.ELECTRICITY_DEVICE,
    // 配网流程信息
    var configLinkInfo: ConfigLinkInfoVO = ConfigLinkInfoVO()
)
