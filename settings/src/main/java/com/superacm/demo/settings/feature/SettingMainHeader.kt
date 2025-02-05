package com.superacm.demo.settings.feature

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.superacm.demo.lib.core.compose.sdp
import com.superacm.demo.settings.R
import com.superacm.demo.settings.SettingModel

@Composable
fun DeviceSettingTitle(
    uiState: SettingModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val roundedCorner = 20.sdp
    val bottomCardPadding = 28.sdp
    val bottomAlignRowHeight = 144.sdp

    Column(
        modifier = modifier
            .padding(
                start = bottomCardPadding,
                end = bottomCardPadding
            )
    )
    {
        Box(
            modifier = Modifier.wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Color.White,
                        RoundedCornerShape(roundedCorner)
                    )
                    .clip(RoundedCornerShape(roundedCorner))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true)
                    ) { onClick() })
            {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(bottomAlignRowHeight)
                            .fillMaxWidth()

                    ) {

                        Spacer(modifier = Modifier.width(140.sdp))
                        ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                            val (left, right) = createRefs()
                            Column(modifier = Modifier.constrainAs(left) {
                                start.linkTo(parent.start)
                                end.linkTo(right.start, 15.dp)
                                width = Dimension.fillToConstraints
                                centerVerticallyTo(parent)
                            }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 12.sdp),
                                        text = uiState.nickName,
                                        style = TextStyle(
                                            color = Color.Black, fontSize = 16.sp
                                        ),
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    when {
//                                        uiState.offline -> OfflineState("离线")
//                                        uiState.sleep -> OfflineState("休眠")
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.sdp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 12.sdp),
                                        text = (if (uiState.sn.isNotEmpty()) "ID: ${uiState.sn}" else ""),
                                        style = TextStyle(
                                            color = Color.Gray, fontSize = 12.sp
                                        ),
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                    )
                                    Image(
                                        modifier = Modifier.size(28.sdp),
                                        painter = painterResource(id = R.drawable.setting_copy),
                                        contentDescription = null
                                    )
                                }
                            }
                        }


                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(20.sdp))
                AsyncImage(
                    model = uiState.icon,
                    modifier = Modifier
                        .size(117.sdp)
                        .background(
                            Color.White,
                            CircleShape
                        ),
                    contentDescription = "expand"
                )
            }
        }
    }
}

@Composable
fun OfflineState(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0x1AFF5348),
                shape = RoundedCornerShape(30.sdp)
            )
            .padding(horizontal = 12.sdp, vertical = 4.sdp)
    ) {
        Text(
            text = text,
            color = Color(0xFFFF5348),
            fontSize = TextUnit(12.0f, TextUnitType.Sp)
        )
    }
}
