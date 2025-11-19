package com.superacm.demo.player

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * AP Mode Test Activity
 * 用于测试 AP 模式直播和点播功能
 */
class ApModeTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ApModeTestScreen(
                        onLiveClick = { apIp, apPort, localIp, clientId ->
                            startApLive(apIp, apPort, localIp, clientId)
                        },
                        onVodClick = { apIp, apPort, localIp, clientId ->
                            startApVod(apIp, apPort, localIp, clientId)
                        },
                        onDownloadClick = { apIp, apPort, localIp, clientId ->
                            startApDownload(apIp, apPort, localIp, clientId)
                        }
                    )
                }
            }
        }
    }

    private fun startApLive(apIp: String, apPort: String, localIp: String, clientId: String) {
        val intent = Intent(this, RMPApLivePlayerActivity::class.java).apply {
            putExtra("ap_ip", apIp)
            putExtra("ap_port", apPort)
            putExtra("local_ip", localIp)
            putExtra("client_id", clientId)
        }
        startActivity(intent)
    }

    private fun startApVod(apIp: String, apPort: String, localIp: String, clientId: String) {
        val intent = Intent(this, RMPApVodPlayerActivity::class.java).apply {
            putExtra("ap_ip", apIp)
            putExtra("ap_port", apPort)
            putExtra("local_ip", localIp)
            putExtra("client_id", clientId)
        }
        startActivity(intent)
    }

    private fun startApDownload(apIp: String, apPort: String, localIp: String, clientId: String) {
        val intent = Intent(this, RMPUnifiedDownloadActivity::class.java).apply {
            putExtra("ap_ip", apIp)
            putExtra("ap_port", apPort)
            putExtra("local_ip", localIp)
            putExtra("client_id", clientId)
        }
        startActivity(intent)
    }
}

@Composable
fun ApModeTestScreen(
    onLiveClick: (String, String, String, String) -> Unit,
    onVodClick: (String, String, String, String) -> Unit,
    onDownloadClick: (String, String, String, String) -> Unit
) {
    var apIp by remember { mutableStateOf("192.168.43.1") }
    var apPort by remember { mutableStateOf("6684") }
    var localIp by remember { mutableStateOf("0.0.0.0") }
    var clientId by remember { mutableStateOf("demo_client") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "AP Mode Test",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        OutlinedTextField(
            value = apIp,
            onValueChange = { apIp = it },
            label = { Text("AP IP") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = apPort,
            onValueChange = { apPort = it },
            label = { Text("AP Port") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = localIp,
            onValueChange = { localIp = it },
            label = { Text("Local IP") },
            placeholder = { Text("0.0.0.0") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = clientId,
            onValueChange = { clientId = it },
            label = { Text("Client ID") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onLiveClick(apIp, apPort, localIp, clientId) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Test AP Live")
        }

        Button(
            onClick = { onVodClick(apIp, apPort, localIp, clientId) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Test AP VOD")
        }

        Button(
            onClick = { onDownloadClick(apIp, apPort, localIp, clientId) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Test AP Download")
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "说明：\n" +
                    "1. 确保设备已连接到 AP 热点\n" +
                    "2. AP IP 默认为 192.168.43.1，端口默认为 6684\n" +
                    "3. Local IP 使用 0.0.0.0 表示自动绑定",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
