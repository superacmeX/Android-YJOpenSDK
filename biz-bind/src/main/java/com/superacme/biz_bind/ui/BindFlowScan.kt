package com.superacme.biz_bind.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.superacme.biz_bind.BindViewModel
import com.superacme.biz_bind.R
import com.superacme.biz_bind.core.LoadingProcess
import com.superacme.biz_bind.core.getActivity
import com.superacme.biz_bind.data.BindFlow
import com.superacme.biz_bind.intent.BindIntent

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun BindFlowScan() {

    val viewModel: BindViewModel = viewModel()

    LaunchedEffect(key1 = null) {
        viewModel.processIntent(BindIntent.UpdatePageState(BindFlow.CameraScan))
    }

    // 纵向布局
    Column(modifier = Modifier.fillMaxSize()) {
        // 容器内容
        BindFlowContentScreen()
    }
}

@Composable
fun BindFlowContentScreen() {
    val viewModel: BindViewModel = viewModel()
    val state = viewModel.pageState.collectAsState().value

    val context = LocalContext.current.getActivity()

    // 如果配置成功，则退出绑定流程
    if (state.isFinish) {
        context?.finish()
    }

    if (state.isLoading) {
        LoadingProcess()
    }

    when (state.pageScreen) {
        is BindFlow.CameraScan -> {
            CameraScanScreen(
                onBarcodeScanned = {
                    if (it.isNotEmpty()) {
                        viewModel.processIntent(BindIntent.UpdateQRValue(it))
                    }
                },
                onBackClick = {
                    context?.finish()
                })
        }

        is BindFlow.GuideReset -> {
            val tipsList = mutableListOf<String>()
            tipsList.add(stringResource(id = R.string.bluetooth_device_reset_tips1))
            tipsList.add(stringResource(id = R.string.bluetooth_device_reset_tips2))
            tipsList.add(stringResource(id = R.string.bluetooth_device_reset_tips3))
            GuideResetScreen(tipsList = tipsList, onBackClick = {
                viewModel.processIntent(BindIntent.UpdatePageState(BindFlow.CameraScan))
            }, onNextClick = {
                viewModel.processIntent(BindIntent.ConfigBindDevice)
            })
        }

        is BindFlow.WifiChoose -> {
            WifiChooseScreen(wifiSsid = state.wifiSsid, ssidOnValueChange = {
                viewModel.processIntent(BindIntent.UpdateWifiSsid(it))
            }, wifiPwd = state.wifiPwd, pwdOnValueChange = {
                viewModel.processIntent(BindIntent.UpdateWifiPwd(it))
            }, onBackClick = {
                viewModel.processIntent(BindIntent.UpdatePageState(BindFlow.GuideReset))
            }, onNextClick = {
                viewModel.processIntent(BindIntent.BindDevice)
            })
        }


        else -> {}
    }
}