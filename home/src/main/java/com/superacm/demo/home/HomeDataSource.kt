package com.superacm.demo.home

import com.superacm.demo.lib.core.model.HomeModel
import com.superacme.common_network.YJGateWay
import com.superacme.common_network.constant.NetResult


interface IHomeNetDataSource {

    suspend fun getDeviceList(): NetResult<HomeModel>
}

class HomeNetDataSource : IHomeNetDataSource {

    override suspend fun getDeviceList() =
        YJGateWay.createApi(HomeApi::class.java).getDeviceList()

}