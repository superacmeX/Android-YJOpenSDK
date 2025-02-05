package com.superacm.demo.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.alibaba.fastjson.JSON
import com.superacm.demo.lib.core.theme.DemoTheme
import com.superacm.demo.home.video.VideoDataListActivity
import com.superacm.demo.lib.core.model.Device
import com.superacm.demo.player.PlayerParam
import com.superacm.demo.player.RMPNetLivePlayerActivity
import com.superacm.demo.settings.SettingsActivity
import com.superacme.biz_bind.BindActivity

class HomeActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoTheme { // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color(0xffeeeeee)),
                        onAddClick = { startBindActivity() },
                        onItemClick = { type, device ->
                            Log.d("HomeScreen", "type: $type, device: $device")
                            when (type) {
                                CLICK_CLOUD, CLICK_CARD -> {
                                    startActivity(Intent(this, VideoDataListActivity::class.java).also {
                                        it.putExtra("page",if(type==CLICK_CLOUD)1 else 2)
                                        it.putExtra("device", JSON.toJSONString(device)) })
                                }

                                CLICK_LIVE -> {
                                    // TODO: 跳转到直播设备列表
                                    startLiveActivity(device)
                                }

                                CLICK_SETTINGS -> {
                                    startSettingsActivity(device)
                                }

                            }
                        }
                    )
                }
            }
        }
    }

    private fun startLiveActivity(device : Device) {
        val intent : Intent = Intent(this, RMPNetLivePlayerActivity::class.java)
        val param : PlayerParam = PlayerParam(device.deviceName, device.productKey)
        intent.putExtra(PlayerParam.PLAY_PARAMS, param);
        startActivity(intent)
    }

    private fun startBindActivity() {
        startActivity(Intent(this, BindActivity::class.java))
    }

    private fun startSettingsActivity(device: Device) {
        startActivity(Intent(this, SettingsActivity::class.java).also {
            it.putExtra("deviceId", device.id)
        })
    }
}

