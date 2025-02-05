package com.superacm.demo.home.video

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.gson.Gson
import com.superacm.demo.home.video.data.DateViewModel
import com.superacm.demo.home.video.ui.DateSelectionPage
import com.superacm.demo.lib.core.model.Device

class VideoDataListActivity : ComponentActivity() {
    val TAG = "andymao->VideoDataListActivity"
    lateinit var deviceBean: Device
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val device = intent.getStringExtra("device")
        val pageType = intent.getIntExtra("page", 1)
        deviceBean = Gson().fromJson(device, Device::class.java)
        Log.d(TAG, "onCreate() called with: deviceBean = $deviceBean")

        enableEdgeToEdge()
        setContent {
            val viewModel = DateViewModel(deviceBean)
            viewModel.pageType = pageType
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                DateSelectionPage(viewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DateSelectionPage(DateViewModel(null))
}