package com.superacm.demo.settings

import com.superacme.common_network.constant.NetResult
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.POST

data class PropertyBody(val deviceId: String, val identifiers: List<String>)

data class PropertySetBody(
    val deviceId: String,
    val method: String,
    val identifier: String,
    val id: String,
    val params: Any = Any()
)



data class PropertyList(
    val devicePropertyList: List<DeviceProperty>
)

data class DeviceProperty(
    val deviceId: String,
    val propertyList: List<Property>
)

data class Property(
    val propertyName: String,
    val propertyType: String
)

fun createProperty(deviceId: String, identifier: String, id: String, params: Any) = PropertySetBody(
    deviceId = deviceId,
    method = "thing.service.property.set",
    identifier = identifier,
    id = id,
    params = params
)

fun createService(deviceId: String, identifier: String, id: String) = PropertySetBody(
    deviceId = deviceId,
    method = "thing.service.${identifier}",
    identifier = identifier,
    id = id
)

data class PropertyModel(
    val deviceName: String,
    val productKey: String,
    val values: List<Value>
)

data class Value(
    val gmtModified: Long,
    val identifier: String,
    val statusValue: Int
)

interface SettingsApi {

    @POST("/operation/api/v1/unified/operation/property/get")
    suspend fun getProperty(@Body body: PropertyBody): NetResult<PropertyModel>

    @POST("/operation/api/v1/unified/operation/down")
    suspend fun setProperty(@Body body: PropertySetBody): NetResult<JSONObject>


    @POST("/user/api/v1/user/devicePropertyList")
    suspend fun devicePropertyList(@Body body: PropertyList): NetResult<List<DevicePropertyBO>>

}

data class PropertyDataBO(
    val propertyName: String,
    val propertyValue: String,
    val propertyType: Int
)

data class DevicePropertyBO(
    val deviceId: String,
    val deviceName: String,
    val propertyData: List<PropertyDataBO>
)