package com.superacm.demo.home

import com.superacm.demo.lib.core.model.HomeModel
import com.superacme.common_network.constant.NetResult


interface IHomeRepository {

    suspend fun getDeviceList(): NetResult<HomeModel>
}


class HomeRepository(private val homeDataSource: IHomeNetDataSource = HomeNetDataSource()) :
    IHomeRepository {

    override suspend fun getDeviceList() = homeDataSource.getDeviceList()
}