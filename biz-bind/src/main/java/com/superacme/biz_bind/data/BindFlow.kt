package com.superacme.biz_bind.data

sealed class BindFlow {
    companion object {
        const val TAG = "WIFIAPFlow"
    }

    // 流程初始化
    data object Default : BindFlow()

    // 相机扫一扫
    data object CameraScan : BindFlow()

    // 重置提示
    data object GuideReset : BindFlow()

    // wifi选择
    data object WifiChoose : BindFlow()

    // 连接确认
    data object ConnectConfirm : BindFlow()

    // 绑定扫描
    data object BindScan : BindFlow()

    // 绑定成功
    data object BindSuccess : BindFlow()

    // 绑定失败
    data object BindFail : BindFlow()
}