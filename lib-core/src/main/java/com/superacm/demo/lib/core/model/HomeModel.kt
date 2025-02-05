package com.superacm.demo.lib.core.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
class HomeModel : ArrayList<HomeModelItem>()

@Keep
data class HomeModelItem(
    val defaultGroup: Boolean,
    val deviceList: List<Device>,
    val groupName: String,
    val id: String
) : Serializable

@Keep
data class Device(
    val activeTime: Long,
    val authorities: List<String>,
    val bindId: String,
    val colorPic: String,
    val devModel: String,
    val deviceName: String,
    val deviceRole: Int,
    val deviceSecret: Any,
    val deviceSource: String,
    val deviceType: String,
    val displayRatio: Int,
    val extendInfo: ExtendInfo,
    val firmwareVersion: String,
    val fromUser: String,
    val gmtCreate: Long,
    val gmtModified: Long,
    val groupId: String,
    val groupName: String,
    val id: String,
    val iotId: String,
    val nickName: String,
    val otaAuto: Int,
    val owned: String,
    val ownerId: String,
    val picUrl: String,
    val productKey: String,
    val productSource: Int,
    val sn: String,
    val status: Int,
    val tenantId: String
) : Serializable

@Keep
data class ExtendInfo(
    val checkSimCard: Boolean,
    val clientSwitch: String,
    val cloudStorageDays: String,
    val cloudStorageFlag: String
) : Serializable