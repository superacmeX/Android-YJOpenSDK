package com.superacme.biz_bind.data.enumerate

enum class LinkType(val type: Int) {
    // 1-AP热点
    WIFI_AP_HOT(1),

    // 2-蓝牙
    BLUETOOTH(2),

    // 3-设备扫码
    CAMERA_SCAN(3),

    // 4-4G
    FOUR_G(4)
}