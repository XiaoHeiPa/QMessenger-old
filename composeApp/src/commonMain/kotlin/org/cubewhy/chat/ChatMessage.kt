package org.cubewhy.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// https://github.com/JetBrains/compose-multiplatform

@Composable
fun Triangle(risingToTheRight: Boolean, background: Color) {
    Box(
        Modifier
            .padding(bottom = 10.dp, start = 0.dp)
            .clip(TriangleEdgeShape(risingToTheRight))
            .background(background)
            .size(6.dp)
    )
}

@Composable
inline fun ChatMessage(isMyMessage: Boolean, message: ChatMessage<*>) {
    val imageLoader =
        ImageLoader.Builder(LocalPlatformContext.current).diskCachePolicy(CachePolicy.DISABLED)
            .memoryCachePolicy(CachePolicy.ENABLED).build()

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMyMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {

        Row(verticalAlignment = Alignment.Bottom) {
            if (!isMyMessage) {
                Column {
                    AsyncImage(
                        modifier = Modifier.clip(CircleShape).size(40.dp),
                        model = "${config.api}/api/avatar/image/${message.sender.username}",
                        contentDescription = "Avatar of channel ${message.sender.username}",
                        imageLoader = imageLoader
                    )
                }
                Spacer(Modifier.size(2.dp))
                Column {
                    Triangle(true, ChatColors.OTHERS_MESSAGE)
                }
            }

            Column {
                Box(
                    Modifier.clip(
                        RoundedCornerShape(
                            10.dp,
                            10.dp,
                            if (!isMyMessage) 10.dp else 0.dp,
                            if (!isMyMessage) 0.dp else 10.dp
                        )
                    )
                        .background(color = if (!isMyMessage) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary)
                        .padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 5.dp),
                ) {
                    Column {
                        if (!isMyMessage) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = message.sender.nickname,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.sp,
                                        fontSize = 14.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                        }
                        Spacer(Modifier.size(3.dp))
                        Text(
                            text = (message.content as BaseMessage).data,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 18.sp,
                                letterSpacing = 0.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.size(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = timeToString(message.timestamp),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 10.sp),
                                color = ChatColors.TIME_TEXT
                            )
                        }
                    }
                }
                Box(Modifier.size(10.dp))
            }
            if (isMyMessage) {
                Column {
                    Triangle(false, MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}


// Adapted from https://stackoverflow.com/questions/65965852/jetpack-compose-create-chat-bubble-with-arrow-and-border-elevation
class TriangleEdgeShape(val risingToTheRight: Boolean) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val trianglePath = if (risingToTheRight) {
            Path().apply {
                moveTo(x = 0f, y = size.height)
                lineTo(x = size.width, y = 0f)
                lineTo(x = size.width, y = size.height)
            }
        } else {
            Path().apply {
                moveTo(x = 0f, y = 0f)
                lineTo(x = size.width, y = size.height)
                lineTo(x = 0f, y = size.height)
            }
        }

        return Outline.Generic(path = trianglePath)
    }
}

object ChatColors {
    val MY_MESSAGE = Color(0xFFE5FEFB)
    val OTHERS_MESSAGE = Color.White
    val TIME_TEXT = Color(0xFF979797)
}

fun timeToString(milliseconds: Long): String {
    val seconds = milliseconds / 1000

    val instant: Instant = Instant.fromEpochSeconds(seconds)

    val localTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    val m = localTime.minute
    val h = localTime.hour

    val mm = if (m < 10) "0$m" else m.toString()
    val hh = if (h < 10) "0$h" else h.toString()

    return "$hh:$mm"
}

