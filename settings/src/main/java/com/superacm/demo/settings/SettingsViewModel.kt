package com.superacm.demo.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.superacme.common.logan.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "SettingsViewModel"

sealed interface SettingUIState {
    data class Success(val model: SettingModel) : SettingUIState

    data object Error : SettingUIState

    data object Loading : SettingUIState
}


data class SettingModel(
    val nickName: String,
    val icon: String,
    val sn: String,
    val volume: Int,
    val propertyList: List<DevicePropertyBO>
)

class SettingsViewModel(private val deviceId: String) : ViewModel() {

    private val repo = SettingsRepo()


    val uiState: StateFlow<SettingUIState> = combine(
        repo.getSettingModel(deviceId),
        repo.getProperty(deviceId, listOf("CallVolume")),
        repo.propertyList(propertyList(deviceId))
    ) { device, property, list ->

        Logger.d(TAG, "combine property: $property")

        SettingUIState.Success(
            SettingModel(
                nickName = device.nickName,
                icon = device.colorPic,
                sn = device.deviceName,
                volume = property.getOrNull()?.values?.find { it.identifier == "CallVolume" }?.statusValue
                    ?: 0,
                propertyList = list.getOrNull() ?: emptyList()
            )
        )

    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingUIState.Loading)


    fun onReboot() {
        viewModelScope.launch {
            repo.operateService(
                deviceId = deviceId,
                identifier = "Reboot",
                id = "${System.currentTimeMillis()}"
            )
        }
    }


    companion object {

        fun provideFactory(deviceId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {

                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(deviceId = deviceId) as T
                }
            }
    }


}


private fun propertyList(deviceId: String) = PropertyList(
    devicePropertyList = listOf(
        DeviceProperty(
            deviceId = deviceId,
            propertyList = listOf(
                Property(propertyName = "displayStatus", propertyType = "1"),
                Property(propertyName = "RemainingBattery", propertyType = "0"),
                Property(propertyName = "WiFiRSSI", propertyType = "0"),
                Property(propertyName = "FourGSignalStrength", propertyType = "0"),
                Property(propertyName = "DeviceSleepSwitch", propertyType = "0")
            )
        ),
    )
)