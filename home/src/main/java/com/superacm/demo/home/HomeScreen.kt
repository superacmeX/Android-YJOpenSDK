package com.superacm.demo.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.superacm.demo.lib.core.compose.TitleBar
import com.superacm.demo.lib.core.compose.TitleText
import com.superacm.demo.lib.core.model.Device
import com.superacm.demo.lib.core.model.HomeModel
import com.superacm.demo.lib.core.theme.Purple40

internal const val CLICK_LIVE = 1
internal const val CLICK_CLOUD = 2
internal const val CLICK_CARD = 3
internal const val CLICK_SETTINGS = 4


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit,
    onItemClick: (Int, Device) -> Unit
) {

    val viewModel: HomeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    HomeScreen(
        modifier = modifier,
        viewModel = viewModel,
        uiState = uiState,
        onAddClick = onAddClick,
        onItemClick = onItemClick
    )

}


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    uiState: HomeUiState,
    onAddClick: () -> Unit,
    onItemClick: (Int, Device) -> Unit
) {

    Column(modifier = modifier) {

        Box(modifier = Modifier.background(color = Color.White)) {
            TitleBar(
                left = { },
                center = {
                    TitleText(text = "设备列表")
                },
                right = {
                    Icon(
                        modifier = Modifier.clickable { onAddClick.invoke() },
                        imageVector = Icons.Default.Add,
                        contentDescription = "add"
                    )
                }
            )
        }
        when (uiState) {
            is HomeUiState.Error -> HomeError(uiState.error ?: "") {
                viewModel.onRefresh()
            }

            HomeUiState.Loading -> {}
            is HomeUiState.Success -> HomeSuccess(
                homeModel = uiState.devices,
                onItemClick = onItemClick
            )
        }
    }


}

@Composable
fun HomeError(error: String, onRefresh: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            modifier = Modifier
                .clickable { onRefresh() },
            text = "Error($error)",
            fontSize = 40.sp
        )
    }
}


@Composable
fun HomeSuccess(homeModel: HomeModel, onItemClick: (Int, Device) -> Unit) {

    if (homeModel.isNotEmpty()) {
        val allGroup = homeModel[0]
        LazyColumn {
            items(allGroup.deviceList) {
                DeviceItem(device = it, onItemClick = onItemClick)
            }
        }
    }


}

@Composable
fun DeviceItem(device: Device, onItemClick: (Int, Device) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)
            .background(color = Color.White, shape = RoundedCornerShape(10.dp))
            .padding(15.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                modifier = Modifier.size(35.dp),
                model = device.colorPic,
                contentDescription = device.nickName
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = device.nickName)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                modifier = Modifier.clickable { onItemClick(CLICK_SETTINGS, device) },
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = Purple40
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HomeIcon("直播", onClick = { onItemClick(CLICK_LIVE, device) })
            HomeIcon("卡录像", onClick = { onItemClick(CLICK_CARD, device) })
            HomeIcon("云录像", onClick = { onItemClick(CLICK_CLOUD, device) })

        }
    }
}

@Composable
fun HomeIcon(txt: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .width(80.dp)
            .border(2.dp, color = Purple40, shape = RoundedCornerShape(10.dp))
            .clickable { onClick.invoke() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = txt)
    }
}


@Preview
@Composable
fun HomeScreenPreview(modifier: Modifier = Modifier) {
    Text(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White), text = "HomeScreen"
    )
}