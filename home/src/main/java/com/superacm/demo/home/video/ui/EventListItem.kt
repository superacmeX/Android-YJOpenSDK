package com.superacm.demo.home.video.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType.Companion.Sp
import com.superacm.demo.home.video.data.MediaRecord
import com.superacm.demo.lib.core.model.Device
import com.superacme.biz_bind.core.sdp

val Int.stu: TextUnit
    get() = TextUnit(
        this.toFloat() / 2,
        Sp
    )


data class EventItemUIData(
    val dateOffsetSec: Long,
    val picUrl: String,
    val timeStr: String,
    val type: Int,
    val device: Device,
    val record: MediaRecord? = null,
)

@Composable
fun EventListItem(
    index: Int,
    editableMsgBean: EventItemUIData,
    playing: Boolean = false,
    checkChange: ((EventItemUIData) -> Unit) = {},
    onClick: ((EventItemUIData) -> Unit) = {},
    onAction: ((EventItemUIData) -> Unit) = {},
    showActionButtons: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick(editableMsgBean)
            }
    ) {
        Column(
            modifier = Modifier
                .padding(start = 75.sdp, top = 12.sdp, bottom = 30.sdp)
                .align(Alignment.Top)
        ) {
            Text(
                modifier = Modifier,
                text = editableMsgBean.timeStr,
                style = TextStyle(
                    color = Color(0xff9B9DB1), fontSize = 28.stu
                ),
            )
        }
        Spacer(modifier = Modifier.weight(0.7f))

        if (editableMsgBean.picUrl.isNotEmpty()) {
            Box(
                Modifier
                    .padding(top = 16.sdp)
                    .width(152.sdp)
                    .height(84.sdp)
                    .clip(RoundedCornerShape(8.sdp))
                    .border(1.sdp, Color.LightGray, RoundedCornerShape(8.sdp)),
            ) {

                EventImage(editableMsgBean.picUrl)
            }
        }
    }

}