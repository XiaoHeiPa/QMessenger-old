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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

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

    channelConf?.let { conf ->
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Channel Configuration", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Nickname:")
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
//                            QMessenger.updateChannelNickname(id, nickname.text)
                            channelConf?.nickname = nickname.text
                            isEditing = false
                        } catch (e: Exception) {
                            errorMessage = "Failed to update nickname: ${e.message}"
                        }
                    }
                } else {
                    isEditing = true
                }
            }) {
                Text(if (isEditing) "Save" else "Edit Nickname")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (channelConf?.permissions?.contains(Permission.MANAGE_CHANNEL) == true) {
                Button(onClick = {
                    // Handle group info update
                    // Example: navigate to another screen to edit channel info
                }) {
                    Text("Edit Channel Info")
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
                Text("Leave Channel")
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    } ?: run {
        // Loading or error state
        Text("Loading channel information...")
    }
}