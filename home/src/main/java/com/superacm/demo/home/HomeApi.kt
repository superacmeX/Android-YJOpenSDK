package com.superacm.demo.home

import com.superacm.demo.lib.core.model.HomeModel
import com.superacme.common_network.constant.NetResult
import retrofit2.http.POST


interface HomeApi {

    @POST("/user/api/v2/allGroupDeviceList")
    suspend fun getDeviceList():NetResult<HomeModel>

}
