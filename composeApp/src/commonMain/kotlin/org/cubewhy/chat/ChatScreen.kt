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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import org.jetbrains.compose.resources.stringResource
import qmessenger.composeapp.generated.resources.Res
import qmessenger.composeapp.generated.resources.channel_tip
import qmessenger.composeapp.generated.resources.close
import qmessenger.composeapp.generated.resources.disconnected
import qmessenger.composeapp.generated.resources.member_count
import qmessenger.composeapp.generated.resources.no_title
import qmessenger.composeapp.generated.resources.user_info

@Composable
fun ChatScreen(nav: NavController, navToChat: (Channel, Account) -> Unit) {
    val channels = remember { mutableStateListOf<Channel>() }
    var currentChannel by remember { mutableStateOf<Channel?>(null) }
    var fold by remember { mutableStateOf(config.fold) }
    val isAndroid = getPlatform().type == PlatformType.ANDROID
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<Account?>(null) }
    var action by remember { mutableStateOf<String?>(null) }
    val imageLoader =
        ImageLoader.Builder(LocalPlatformContext.current).diskCachePolicy(CachePolicy.DISABLED)
            .memoryCachePolicy(CachePolicy.ENABLED).build()

    if (user == null) {
        loadUser(scope) {
            user = it
        }
    }

    if (channels.isEmpty()) {
        // 不知道为什么重载的时候这里会被多次执行
        loadChannels(scope, channels)
    }

    Row(modifier = Modifier.fillMaxSize()) {
        if (!isAndroid || (isAndroid && currentChannel == null)) {
            Box(modifier = Modifier.padding(5.dp).fillMaxHeight()) {
                Row(modifier = Modifier.align(Alignment.TopStart)) {
                    IconButton(onClick = {
                        fold = !fold
                        config.fold = fold
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Menu, contentDescription = "Menu"
                        )
                    }
                    AnimatedVisibility(!fold) {
                        var showUserInfo by remember { mutableStateOf(false) }
                        AnimatedVisibility(visible = showUserInfo) {
                            UserInfoDialog(userInfo = user, onDismiss = { showUserInfo = false })
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
                    items(channels, key = {
                        it.id
                    }) { channel ->
                        val isCurrent = currentChannel != null && currentChannel!!.id == channel.id
                        Row(
                            modifier = Modifier.clickable {
                                currentChannel = channel
                                action = "CHAT"
                            }.clip(RoundedCornerShape(5.dp)).animateContentSize(
                                animationSpec = tween(durationMillis = 300)
                            )
                                .background(if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background)
                        ) {
                            AsyncImage(
                                modifier = Modifier.size(50.dp, 50.dp).clip(CircleShape),
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
                    IconButton(onClick = {
                        action = "ADD_CONTACT"
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Add, contentDescription = "Add contact"
                        )
                    }
                    AnimatedVisibility(!fold) {
                        IconButton(onClick = {

                        }) {
                            Icon(
                                imageVector = Icons.Filled.Settings, contentDescription = "Settings"
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
                navToChat(currentChannel!!, user!!)
                currentChannel = null
                saveConfig(config)
            }
        } else {
            AnimatedVisibility(
                visible = currentChannel != null && action == "CHAT",
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
                    if (user == null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = stringResource(Res.string.disconnected).replace(
                                    "*server*", config.serverUrl
                                )
                            )
                        }
                    } else {
                        MessageScreen(it, user!!) {
                            currentChannel = null
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = action == "ADD_CONTACT",
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
                IconButton(modifier = Modifier.align(Alignment.TopStart), onClick = {
                    action = null
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                AddContact(modifier = Modifier.fillMaxSize().align(Alignment.Center)) {
                    currentChannel = null
                    loadChannels(scope, channels)
                }
            }

        }
        AnimatedVisibility(
            visible = action == null, enter = slideInVertically(initialOffsetY = { it }) + fadeIn(
                animationSpec = tween(
                    durationMillis = 300
                )
            ), exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(
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

private fun loadUser(
    scope: CoroutineScope, ok: (Account) -> Unit
) {
    scope.launch {
        QMessenger.user().let {
            if (it.isSuccess) {
                ok(it.getOrThrow())
            } else {
                it.exceptionOrNull()?.let { it1 -> println(it1) }
            }
        }
    }
}

private fun loadChannels(
    scope: CoroutineScope, channels: SnapshotStateList<Channel>
) {
    scope.launch {
        QMessenger.channels().let {
            if (it.isSuccess) {
                channels.clear()
                channels.addAll(it.getOrThrow())
            } else {
                it.exceptionOrNull()?.let { it1 -> println(it1) }
            }
        }
    }
}

class MessageViewModel : ViewModel() {
    private val _messages = mutableStateMapOf<Long, SnapshotStateList<ChatMessage<*>>>()

    fun addMessage(msg: ChatMessage<*>) {
        with(_messages[msg.id]) {
            // IDEA的静态检测有问题,这么写只是为了绕过静态检测,实际上 this == null 就可以了
            if (this.isNullOrEmpty()) {
                _messages[msg.id] = mutableStateListOf(msg)
            } else {
                this.add(msg)
            }
        }
    }

    operator fun get(id: Long): SnapshotStateList<ChatMessage<*>> {
        return _messages[id] ?: mutableStateListOf()
    }

    fun addAll(messages: List<ChatMessage<BaseMessage>>) {
        for (message in messages) {
            addMessage(message)
        }
    }
}

@Composable
fun MessageScreen(channel: Channel, user: Account, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // 存在连接就不会做任何事情,不存在则开启连接
        QMessenger.websocket(user, channel)?.let {
            for (wsMessage in it.incoming) {
                when (wsMessage) {
                    is Frame.Text -> {
                        val response: WebsocketResponse<JsonObject> =
                            JSON.decodeFromString(wsMessage.readText())
                        if (response.method == WebsocketResponse.NEW_MESSAGE) {
                            val msg: ChatMessage<BaseMessage> =
                                JSON.decodeFromJsonElement(response.data!!)
                            if (msg.sender.id != user.id) {
                                pushNotification(
                                    msg.channel.title ?: msg.channel.name, msg.shortContent
                                )
                            }
                            store.send(
                                Action.SendMessage(
                                    msg
                                )
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
                    IconButton(modifier = Modifier.padding(
                        vertical = 10.dp, horizontal = 5.dp
                    ), onClick = { onDismiss() }) {
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
                                "*count*", channel.memberCount.toString()
                            )
                        )
                    }
                }

                Column(modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.Settings, contentDescription = "Settings"
                        )
                    }
                }
            }
            HorizontalDivider()
            Conversation(user = user, channel = channel)
        }
        ChatBox(modifier = Modifier.align(Alignment.BottomStart).padding(10.dp),
            sendMessage = {
                scope.launch {
                    QMessenger.sendMessage(it, channel, user).let {
                        if (it.isFailure) {
                            it.exceptionOrNull()?.let { it1 -> println(it1) }
                        }
                    }
                }
            })
    }
}

val store = CoroutineScope(SupervisorJob()).createStore()

@Composable
fun Conversation(modifier: Modifier = Modifier, user: Account, channel: Channel) {
    val scope = rememberCoroutineScope()
    val state by store.stateFlow.collectAsState()
    if (state.messages.isEmpty() || state.messages.last().channel.id != channel.id) {
        scope.launch {
            QMessenger.messages(channel, 0).let {
                if (it.isSuccess) {
                    val res = it.getOrThrow()
                    if (res.code == 200) {
                        for (msg in res.data!!.reversed()) {
                            if (!state.messages.contains(msg)) {
                                store.send(Action.SendMessage(msg))
                            }
                        }
                    }
                }
            }
        }
    }
    val listState = rememberLazyListState()
    if (state.messages.isNotEmpty()) {
        LaunchedEffect(state.messages.last()) {
            listState.animateScrollToItem(state.messages.lastIndex, scrollOffset = 2)
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(start = 4.dp, end = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = listState,
    ) {
        item { Spacer(Modifier.size(20.dp)) }
        items(state.messages.filter { it.channel.id == channel.id }, key = { it.id }) {
            ChatMessage(isMyMessage = it.sender.id == user.id, message = it)
        }
        item {
            Box(Modifier.height(70.dp))
        }
    }
}

@Composable
fun ChatBox(
    modifier: Modifier = Modifier, sendMessage: (String) -> Unit
) {
    var textState by remember { mutableStateOf(TextFieldValue()) }
    val focusRequester = FocusRequester()

    TextField(
        modifier = modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp)
            .focusRequester(focusRequester)
            .onKeyEvent { event ->
                when {
                    event.key == Key.Enter && event.isShiftPressed -> {
                        // Handle Shift + Enter (New Line)
                        val newText = textState.text + "\n"
                        val newCursorPosition = TextRange(newText.length)
                        textState = TextFieldValue(newText, newCursorPosition)
                        true
                    }

                    event.key == Key.Enter -> {
                        // Handle Enter (Send Message)
                        sendMessage(textState.text)
                        textState = TextFieldValue("")
                        true
                    }

                    else -> false
                }
            },
        colors = TextFieldDefaults.colors(),
        value = textState,
        placeholder = {
            Text("Type message...")
        },
        onValueChange = {
            textState = it
        },
        trailingIcon = {
            if (textState.text.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .clickable {
                            sendMessage(textState.text)
                            textState = TextFieldValue("")
                        }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Send")
                }
            }
        }
    )
}

@Composable
fun UserInfoDialog(
    userInfo: Account?, onDismiss: () -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss, title = {
        Text(text = stringResource(Res.string.user_info))
    }, text = {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            if (userInfo == null) {
                Text(
                    stringResource(Res.string.disconnected).replace(
                        "*server*", config.serverUrl
                    )
                )
            } else {
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
        }
    }, confirmButton = {
        TextButton(onClick = onDismiss) {
            Text(stringResource(Res.string.close))
        }
    })
}
