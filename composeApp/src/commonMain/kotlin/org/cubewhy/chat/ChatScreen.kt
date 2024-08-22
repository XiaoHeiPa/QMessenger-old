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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import org.jetbrains.compose.resources.stringResource
import qmessenger.composeapp.generated.resources.Res
import qmessenger.composeapp.generated.resources.channel_tip
import qmessenger.composeapp.generated.resources.close
import qmessenger.composeapp.generated.resources.member_count
import qmessenger.composeapp.generated.resources.no_title
import qmessenger.composeapp.generated.resources.user_info

@Composable
fun ChatScreen(nav: (Channel, Account) -> Unit) {
    val channels = remember { mutableStateListOf<Channel>() }
    var currentChannel by remember { mutableStateOf<Channel?>(null) }
    var fold by remember { mutableStateOf(config.fold) }
    val isAndroid = getPlatform().type == PlatformType.ANDROID
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<Account?>(null) }
    val imageLoader = ImageLoader.Builder(LocalPlatformContext.current)
        .diskCachePolicy(CachePolicy.DISABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .build()

    if (user == null) {
        scope.launch {
            QMessenger.user(config.user!!.token).let {
                if (it.isSuccess) {
                    user = it.getOrThrow()
                }
            }
        }
    }

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
            Box(modifier = Modifier.padding(5.dp).fillMaxHeight()) {
                Row(modifier = Modifier.align(Alignment.TopStart)) {
                    IconButton(
                        onClick = {
                            fold = !fold
                            config.fold = fold
                            saveConfig(config)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu"
                        )
                    }
                    AnimatedVisibility(!fold) {
                        var showUserInfo by remember { mutableStateOf(false) }
                        AnimatedVisibility(visible = showUserInfo) {
                            UserInfoDialog(
                                userInfo = user!!,
                                onDismiss = { showUserInfo = false }
                            )
                        }
                        IconButton(onClick = {
                            showUserInfo = true
                        }) {
                            AsyncImage(
                                modifier = Modifier.clip(CircleShape),
                                model = "${config.api}/api/avatar/image/${user?.username}",
                                contentDescription = "Avatar of channel ${user?.username}",
                                imageLoader = imageLoader
                            )
                        }
                    }
                }

                LazyColumn(modifier = Modifier.align(Alignment.CenterStart)) {
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
                                contentDescription = "Avatar of channel ${channel.name}",
                                imageLoader = imageLoader
                            )
                            AnimatedVisibility(!fold) {
                                Column(modifier = Modifier.padding(5.dp)) {
                                    Text(
                                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                                        text = channel.title ?: channel.name
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }
                Row(modifier = Modifier.align(Alignment.BottomStart)) {
                    IconButton(
                        onClick = {

                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add contact"
                        )
                    }
                    AnimatedVisibility(!fold) {
                        IconButton(
                            onClick = {

                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                }
            }
            VerticalDivider()
        }
        // current conversation
        if (isAndroid) {
            if (currentChannel != null) {
                nav(currentChannel!!, user!!)
                currentChannel = null
            }
        } else {
            AnimatedVisibility(
                visible = currentChannel != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 300
                    )
                ),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 300
                    )
                )
            ) {
                currentChannel?.let {
                    MessageScreen(it, user!!) {
                        currentChannel = null
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = currentChannel == null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(
                animationSpec = tween(
                    durationMillis = 300
                )
            ),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(
                animationSpec = tween(
                    durationMillis = 300
                )
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(Res.string.channel_tip)
                )
            }
        }
    }
}

@Composable
fun MessageScreen(channel: Channel, user: Account, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        QMessenger.websocket()?.apply {
            for (wsMessage in incoming) {
                when (wsMessage) {
                    is Frame.Text -> {
                        val response: WebsocketResponse<JsonObject> =
                            JSON.decodeFromString(wsMessage.readText())
                        if (response.method == WebsocketResponse.NEW_MESSAGE) {
                            val msg: ChatMessage<BaseMessage> =
                                JSON.decodeFromJsonElement(response.data!!)
                            pushNotification(
                                msg.channel.title ?: msg.channel.name,
                                msg.shortContent
                            )
                        }
                    }

                    else -> {
                        // unknown type
                    }
                }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().align(Alignment.Center)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.align(Alignment.TopStart)) {
                    IconButton(
                        modifier = Modifier.padding(
                            vertical = 10.dp,
                            horizontal = 5.dp
                        ),
                        onClick = { onDismiss() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = channel.title ?: stringResource(Res.string.no_title)
                        )
                        Text(
                            color = Color.Gray,
                            text = stringResource(Res.string.member_count).replace(
                                "*count*",
                                channel.memberCount.toString()
                            )
                        )
                    }
                }
                Column(modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            }
            HorizontalDivider()
            Conversation()
        }
        ChatBox(
            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp),
            onMenuClicked = {},
            onSendMessageClicked = {
                scope.launch {
                    QMessenger.sendMessage(it, channel, user)
                }
            }
        )
    }
}

@Composable
fun Conversation(modifier: Modifier = Modifier) {
    Text("Hello World")
}

@Composable
fun ChatBox(
    modifier: Modifier = Modifier,
    onMenuClicked: () -> Unit,
    onSendMessageClicked: (String) -> Unit
) {
    var chatBoxValue by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var btnMethodIsSend by remember {
        mutableStateOf(false)
    }
    Row(modifier = modifier) {
        TextField(
            value = chatBoxValue,
            onValueChange = { newValue ->
                chatBoxValue = newValue // update text
                btnMethodIsSend = newValue.text.isNotEmpty()
            },
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            placeholder = {
                Text(text = "Typing...")
            }
        )
        IconButton(
            onClick = {
                val msg = chatBoxValue.text
                if (msg.isBlank()) return@IconButton
                if (btnMethodIsSend) {
                    onSendMessageClicked(chatBoxValue.text)
                } else {
                    onMenuClicked()
                }
                chatBoxValue = TextFieldValue("") // clear text field
            },
            modifier = Modifier
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.onPrimary)
                .align(Alignment.CenterVertically)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            onMenuClicked()
                        }
                    )
                }
        ) {
            Icon(
                imageVector = if (btnMethodIsSend) Icons.AutoMirrored.Filled.Send else Icons.Filled.Add,
                contentDescription = "Function btn",
                modifier = Modifier
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun UserInfoDialog(
    userInfo: Account,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(Res.string.user_info))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "ID: ${userInfo.id}",
                    style = TextStyle(fontSize = 16.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Nickname: ${userInfo.nickname}",
                    style = TextStyle(fontSize = 16.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Username: ${userInfo.username}",
                    style = TextStyle(fontSize = 16.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Bio: ${userInfo.bio}",
                    style = TextStyle(fontSize = 16.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.close))
            }
        }
    )
}
