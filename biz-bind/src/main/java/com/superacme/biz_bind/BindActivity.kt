package com.superacme.biz_bind

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.superacme.biz_bind.ui.BindFlowScan

class BindActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BindFlowScan()
        }
    }
}