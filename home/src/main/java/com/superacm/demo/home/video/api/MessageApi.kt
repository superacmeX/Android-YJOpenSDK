package com.superacm.demo.home.video.api

import com.alibaba.fastjson.JSONObject
import com.superacm.demo.home.video.data.EventListBean
import retrofit2.http.Body
import retrofit2.http.POST

interface MessageApi {

    @POST("/message/api/v1/event/query")
    fun getEventList(@Body body: JSONObject): retrofit2.Call<com.superacme.common_network.constant.NetResult<EventListBean>>

    @POST("/operation/api/v1/unified/operation/down")
    fun sendProperty(@Body body: JSONObject):  retrofit2.Call<com.superacme.common_network.constant.NetResult<JSONObject>>
}