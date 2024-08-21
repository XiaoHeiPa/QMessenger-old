package org.cubewhy.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import qmessenger.composeapp.generated.resources.Res
import qmessenger.composeapp.generated.resources.no_title

@Composable
fun ChatScreen(nav: NavController) {
    var channels = remember { mutableStateListOf<Channel>() }
    var currentChannel by remember { mutableStateOf<Channel?>(null) }
    var fold by remember { mutableStateOf(false) }
    val isAndroid = getPlatform().type == PlatformType.ANDROID
    val scope = rememberCoroutineScope()

    if (channels.isEmpty()) {
        // 不知道为什么重载的时候这里会被多次执行
        scope.launch {
            QMessenger.channels().let {
                if (it.isSuccess) {
                    channels.clear()
                    channels.addAll(it.getOrThrow())
                }
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        if (!isAndroid || (isAndroid && currentChannel == null)) {
            Column(modifier = Modifier.padding(5.dp)) {
                IconButton(
                    onClick = {
                        fold = !fold
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu"
                    )
                }
                LazyColumn {
                    items(
                        channels,
                        key = {
                            it.id
                        }
                    ) { channel ->
                        val isCurrent = currentChannel != null && currentChannel!!.id == channel.id
                        Row(modifier = Modifier.clickable { currentChannel = channel }
                            .clip(RoundedCornerShape(5.dp))
                            .animateContentSize(
                                animationSpec = tween(durationMillis = 300)
                            )
                            .background(if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background)) {
                            AsyncImage(
                                modifier = Modifier.size(50.dp, 50.dp)
                                    .clip(CircleShape),
                                model = "${config.api}/api/avatar/image/${channel.name}",
                                contentDescription = "Avatar of channel ${channel.name}"
                            )
                            if (!fold) {
                                Column(modifier = Modifier.padding(5.dp)) {
                                    Text(
                                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                                        text = channel.name
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }
            }
            VerticalDivider()
        }
        // current conversation
        AnimatedVisibility(
            visible = currentChannel != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            currentChannel?.let {
                Column {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.align(Alignment.TopStart)) {
                            // title
                            Text(text = it.title ?: stringResource(Res.string.no_title))
                            Text(
                                color = Color.Gray,
                                text = it.description
                            )
                        }
                        Column(modifier = Modifier.align(Alignment.TopEnd)) {
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "Settings"
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
            if (currentChannel == null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Click a channel to start"
                    )
                }
            }
        }
    }
}