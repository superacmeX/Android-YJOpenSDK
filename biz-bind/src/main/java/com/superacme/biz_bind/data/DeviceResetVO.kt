package com.superacme.biz_bind.data


data class ConfigLinkInfoVO(
    val deviceReset: DeviceResetVO = DeviceResetVO()
)

data class DeviceResetVO(
    val title: String = "",
    val subTitle: String = "",
    val descTitle: String = "",
    val agreeTip: String = "",
    val tipIconList: MutableList<String> = mutableListOf(),
    val tipList: MutableList<String> = mutableListOf()
)