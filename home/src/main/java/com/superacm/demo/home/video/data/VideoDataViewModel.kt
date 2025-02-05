package com.superacm.demo.home.video.data

import android.content.Context
import android.content.Intent
import com.superacm.demo.home.video.ui.EventItemUIData
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.superacm.demo.home.video.api.MessageApi
import com.superacm.demo.lib.core.model.Device
import com.superacm.demo.player.PlayerParam
import com.superacm.demo.player.RMPNetVodPlayerActivity
import com.superacme.common_network.YJGateWay
import com.superacme.common_network.constant.NetResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.function.Consumer

data class MediaRecord(val type: Int, val startSeconds: Long, val endTimeSeconds: Long)
class DateViewModel(private val deviceBean: Device?) : ViewModel() {
    var pageType: Int = 1
    val TAG = "andymao->DateViewModel"
    val dataLoading = mutableStateOf(false)
    val selectedDate = mutableStateOf(getCurrentDate())  // Store the selected date
    val itemList =
        mutableStateOf(listOf<EventItemUIData>())  // List of items (can change based on the date)
    val defaultFormatStr2 = "yyyy-MM-dd"

    // Function to get current date in a readable format
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Update the date
    fun updateDate(newDate: String) {
        selectedDate.value = newDate
//        itemList.value = getItemList(newDate)  // Refresh the list based on selected date
    }


    fun getDateStartTime(date: Date): Long {
        return Calendar.getInstance().let {
            it.time = date
            it.set(Calendar.HOUR_OF_DAY, 0)
            it.set(Calendar.MINUTE, 0)
            it.set(Calendar.SECOND, 0)
            it.time.time
        }
    }

    fun parseStrDate(input: String, formatStr: String = defaultFormatStr2): Date? {
        val format = SimpleDateFormat(formatStr, Locale.getDefault())
        return format.parse(input)
    }

    fun refreshItemList() {
        if (pageType == 1) {
            refreshCloudVideo()
        } else {
            refreshCardVideoList()
        }
    }

    private fun refreshCloudVideo() {
        val selectedDate = selectedDate.value
        Log.d(TAG, "refreshItemList() called $deviceBean, selectedDate=$selectedDate")
        val deviceBean = deviceBean ?: return
        val jsonObject = JSONObject()

        val date = parseStrDate(selectedDate, defaultFormatStr2)!!
        val beginTime = getDateStartTime(date)
        val endTime = getDateStartTime(date) + 24 * 60 * 60 * 1000 - 1

        jsonObject["beginTime"] = beginTime
        jsonObject["endTime"] = endTime
        jsonObject["deviceIds"] = arrayOf(deviceBean.id)
        dataLoading.value = true
        val eventListData =
            YJGateWay.createApi(MessageApi::class.java).getEventList(jsonObject).enqueue(object :
                Callback<NetResult<EventListBean>> {
                override fun onResponse(
                    call: Call<NetResult<EventListBean>>,
                    response: Response<NetResult<EventListBean>>
                ) {
                    dataLoading.value = false
                    val successful = response.isSuccessful
                    val code = response.body()?.code
                    val codeIsSuccess = code == 0
                    //                    Log.d(TAG, "successful=$successful, code=$code, response=$response")

                    response.body()?.data?.let {

                        val uiDataList = it.eventQueryRespDTOs.map { it ->
                            EventItemUIData(
                                dateOffsetSec = beginTime/1000,
                                picUrl = it.picUrl ?: "",
                                timeStr = it.eventTime,
                                type = pageType,
                                device = deviceBean
                            )
                        }

                        itemList.value = uiDataList
                    }
                    //                    itemList.value = getItemList(newDate)
                    //                callback?.invoke(
                    //                    successful && codeIsSuccess,
                    //                    response.body()?.message ?: ""
                    //                )
                }

                override fun onFailure(call: Call<NetResult<EventListBean>>, throwable: Throwable) {
                    Log.d(TAG, "onFailure() called with: call = $call, throwable = $throwable")
                    //                callback?.invoke(false, throwable.message.toString())
                    dataLoading.value = false
                }
            })
    }

    private fun refreshCardVideoList() {
        val selectedDate = selectedDate.value
        Log.d(TAG, "refreshCardVideoList() called $deviceBean, selectedDate=$selectedDate")
        val deviceBean = deviceBean ?: return
        val jsonObject = JSONObject()

        val date = parseStrDate(selectedDate, defaultFormatStr2)!!
        val beginTime = getDateStartTime(date)
        val endTime = getDateStartTime(date) + 24 * 60 * 60 * 1000 - 1

        val serviceName = "QueryRecordTimeListByPage"

        jsonObject["productKey"] = deviceBean.productKey
        jsonObject["deviceName"] = deviceBean.deviceName
        jsonObject["method"] = "thing.service.$serviceName"
        jsonObject["identifier"] = serviceName
        jsonObject["id"] = UUID.randomUUID().toString()

        val params: MutableMap<String, Any> = HashMap<String, Any>()
        params["PageSize"] = 1000
        params["PageNo"] = 1
        params.put("BeginTime", beginTime / 1000)
        params.put("EndTime", endTime / 1000)
        params.put("Type", 99)

        jsonObject["params"] = params
        dataLoading.value = true
        val eventListData =
            YJGateWay.createApi(MessageApi::class.java).sendProperty(jsonObject).enqueue(object :
                Callback<NetResult<JSONObject>> {
                override fun onResponse(
                    call: Call<NetResult<JSONObject>>,
                    response: Response<NetResult<JSONObject>>
                ) {
                    dataLoading.value = false
                    val successful = response.isSuccessful
                    val code = response.body()?.code
                    val codeIsSuccess = code == 0

                    Log.d(
                        TAG,
                        "refreshCardVideoList successful=$successful, code=$code, response=$response"
                    )

                    // 结束并发请求 >>>
                    if (response.body()?.data?.containsKey("TimeList") == false) return
                    val timeList: JSONArray =
                        response.body()?.data?.getJSONArray("TimeList") ?: JSONArray()
                    val mediaRecordMutableList = mutableListOf<MediaRecord>()
                    timeList.forEach(Consumer<Any> { item: Any ->
                        try {
                            // x_x_x_x: Hour_Type_Start_Duration
                            val s =
                                (item as String).split("_".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            val start = s[0].toLong() * 60 * 60 + s[2].toLong()
                            mediaRecordMutableList.add(
                                MediaRecord(
                                    type = s[1].toInt(),
                                    startSeconds = start,
                                    endTimeSeconds = start + (s[3].toLong())
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    })


                    val uiDataList = mediaRecordMutableList.map {
                        val defaultFormatStr = "yyyy-MM-dd HH:mm:ss"
                        val sdf = SimpleDateFormat(defaultFormatStr, Locale.getDefault())

                        val startTimeStr = sdf.format(Date(beginTime + it.startSeconds * 1000))
                        val endTimeStr = sdf.format(Date(beginTime + it.endTimeSeconds * 1000))

                        EventItemUIData(
                            dateOffsetSec = beginTime/1000,
                            picUrl = "",
                            timeStr = "$startTimeStr - $endTimeStr",
                            type = pageType,
                            device = deviceBean,
                            record = it
                        )
                    }
                    itemList.value = uiDataList
//                    itemList.value = getItemList(newDate)
//                callback?.invoke(
//                    successful && codeIsSuccess,
//                    response.body()?.message ?: ""
//                )
                }

                override fun onFailure(call: Call<NetResult<JSONObject>>, throwable: Throwable) {
                    Log.d(
                        TAG,
                        "refreshCardVideoList onFailure() called with: call = $call, throwable = $throwable"
                    )
                    dataLoading.value = false
//                callback?.invoke(false, throwable.message.toString())
                }
            })

        eventListData.toString()


    }

    fun handleClick(ctx : Context, item: EventItemUIData) {
        if (item.type == 2) {
            val intent : Intent = Intent(ctx, RMPNetVodPlayerActivity::class.java)
            val param : PlayerParam = PlayerParam(item.device.deviceName, item.device.productKey)

            val record = item.record
            if (record != null) {
                param.setTimeRange(item.dateOffsetSec + item.record.startSeconds, item.dateOffsetSec + item.record.endTimeSeconds);
            }

            intent.putExtra(PlayerParam.PLAY_PARAMS, param);
            ctx.startActivity(intent)
        }
    }
}
