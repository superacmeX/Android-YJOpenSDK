package com.superacm.demo.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.superacm.demo.lib.core.cache.MemoryCache
import com.superacm.demo.lib.core.model.HomeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class HomeViewModel(private val homeRepository: IHomeRepository = HomeRepository()) : ViewModel() {

    private val viewModelState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    val uiState: StateFlow<HomeUiState> = viewModelState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = viewModelState.value
        )

    init {
        getDeviceList()
    }


    private fun getDeviceList() {
        viewModelScope.launch {
            val result = homeRepository.getDeviceList()
            if (result.isSuccess && result.data != null) {
                val homeModel = result.data!!
                saveDeviceToCache(homeModel)
                viewModelState.update { HomeUiState.Success(devices = homeModel) }
            } else {
                viewModelState.update { HomeUiState.Error(result.message) }
            }
        }
    }

    private fun saveDeviceToCache(homeModel: HomeModel) {
        if (homeModel.isNotEmpty()) {
            homeModel[0].deviceList.forEach {
                MemoryCache.saveDevice(it.id, it)
            }
        }
    }

    fun onRefresh() {
        getDeviceList()
    }
}


sealed interface HomeUiState {
    data class Success(val devices: HomeModel) : HomeUiState

    data class Error(val error: String?) : HomeUiState

    data object Loading : HomeUiState
}