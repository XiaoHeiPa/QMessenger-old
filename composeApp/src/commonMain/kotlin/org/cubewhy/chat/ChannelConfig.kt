package org.cubewhy.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import qmessenger.composeapp.generated.resources.Res
import qmessenger.composeapp.generated.resources.channel_config
import qmessenger.composeapp.generated.resources.channel_config_loading
import qmessenger.composeapp.generated.resources.channel_nickname
import qmessenger.composeapp.generated.resources.edit_channel_info
import qmessenger.composeapp.generated.resources.edit_channel_nickname
import qmessenger.composeapp.generated.resources.leave_channel
import qmessenger.composeapp.generated.resources.save

@Composable
fun ChannelConfig(nav: NavController, id: Long) {
    val scope = rememberCoroutineScope()
    var channelConf by remember { mutableStateOf<ChannelConfInfo?>(null) }
    var nickname by remember { mutableStateOf(TextFieldValue("")) }
    var isEditing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(id) {
        // Fetch channel configuration
        val conf = QMessenger.channelConf(id)
        conf.let {
            if (it.isSuccess) {
                val response = it.getOrThrow()
                if (response.code == 200) {
                    val conf1 = response.data!!
                    channelConf = conf1
                    nickname = TextFieldValue(conf1.nickname)
                }
            }
        }

    }

    channelConf?.let {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.channel_config),
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(Res.string.channel_nickname))
            TextField(
                value = nickname,
                onValueChange = { nickname = it },
                enabled = isEditing
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (isEditing) {
                    // Handle nickname update
                    scope.launch {
                        try {
                            QMessenger.updateChannelNickname(id, nickname.text).let {
                                if (it.isSuccess) {
                                    val response = it.getOrThrow()
                                    if (response.code == 200) {
                                        channelConf?.nickname = response.data!!.nickname
                                        isEditing = false
                                        store.removeAll()
                                    } else {
                                        errorMessage = response.message
                                    }
                                } else {
                                    throw it.exceptionOrNull()!!
                                }
                            }
                        } catch (e: Exception) {
                            errorMessage = "Failed to update nickname: ${e.message}"
                        }
                    }
                } else {
                    isEditing = true
                }
            }) {
                Text(stringResource(if (isEditing) Res.string.save else Res.string.edit_channel_nickname))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (channelConf?.permissions?.contains(Permission.MANAGE_CHANNEL) == true) {
                Button(onClick = {
                    // Handle group info update
                    // Example: navigate to another screen to edit channel info
                }) {
                    Text(stringResource(Res.string.edit_channel_info))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                // Handle leave channel
                scope.launch {
                    try {
//                        QMessenger.leaveChannel(id)
                        nav.popBackStack() // Go back after leaving the channel
                    } catch (e: Exception) {
                        errorMessage = "Failed to leave channel: ${e.message}"
                    }
                }
            }) {
                Text(
                    color = Color.Red,
                    text = stringResource(Res.string.leave_channel)
                )
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    } ?: run {
        // Loading or error state
        Text(stringResource(Res.string.channel_config_loading))
    }
}