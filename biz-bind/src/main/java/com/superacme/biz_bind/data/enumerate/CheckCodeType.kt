package com.superacme.biz_bind.data.enumerate

enum class CheckCodeType(val type: Int) {
    // 0-未绑定
    UNBIND(0),

    // 1-已绑定
    BIND(1),

    // 2-被自己绑定
    BIND_BY_SELF(2),

    // 3-设备不存在
    DEVICE_NOT_EXIST(3)
}
