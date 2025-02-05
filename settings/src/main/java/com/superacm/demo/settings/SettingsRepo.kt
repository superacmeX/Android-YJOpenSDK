package com.superacm.demo.settings

import com.superacm.demo.lib.core.cache.MemoryCache
import com.superacm.demo.lib.core.model.Device
import com.superacme.common.logan.Logger
import com.superacme.common_network.YJGateWay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface ISettingsRepo {

    fun getSettingModel(deviceId: String): Flow<Device>

    fun getProperty(deviceId: String, identifiers: List<String>): Flow<Result<PropertyModel>>

    fun propertyList(propertyList: PropertyList): Flow<Result<List<DevicePropertyBO>>>

    suspend fun setProperty(deviceId: String, identifier: String, id: String, params: Any)

    suspend fun operateService(deviceId: String, identifier: String, id: String)

}


class SettingsRepo : ISettingsRepo {

    override fun getSettingModel(deviceId: String): Flow<Device> = flow {
        val model = MemoryCache.getDevice(deviceId)!!
        emit(model)
    }.flowOn(Dispatchers.IO)

    override fun getProperty(
        deviceId: String,
        identifiers: List<String>
    ): Flow<Result<PropertyModel>> = flow {
        val property =
            YJGateWay.createApi(SettingsApi::class.java)
                .getProperty(PropertyBody(deviceId, identifiers))
        Logger.d("SettingsViewModel", "getProperty: ${property.data}")
        if (property.isSuccess) {
            emit(Result.success(property.data!!))
        } else {
            emit(Result.failure(Exception("request failed")))
        }
    }.flowOn(Dispatchers.IO)

    override fun propertyList(propertyList: PropertyList): Flow<Result<List<DevicePropertyBO>>> =
        flow {
            val result = YJGateWay.createApi(SettingsApi::class.java)
                .devicePropertyList(propertyList)
            if (result.isSuccess && result.data != null) {
                emit(Result.success(result.data!!))
            } else {
                emit(Result.failure(Exception("request failed or data is null")))
            }

        }.catch { emit(Result.failure(Exception(it))) }
            .flowOn(Dispatchers.IO)


    override suspend fun setProperty(
        deviceId: String,
        identifier: String,
        id: String,
        params: Any
    ) {
        YJGateWay.createApi(SettingsApi::class.java).setProperty(
            createProperty(
                deviceId = deviceId,
                identifier = identifier,
                id = id,
                params = params
            )
        )
    }

    override suspend fun operateService(deviceId: String, identifier: String, id: String) {
        YJGateWay.createApi(SettingsApi::class.java).setProperty(
            createService(deviceId = deviceId, identifier = identifier, id = id)
        )
    }
}