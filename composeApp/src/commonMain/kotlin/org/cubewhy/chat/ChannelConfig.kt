package org.cubewhy.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RemoveModerator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import qmessenger.composeapp.generated.resources.Res
import qmessenger.composeapp.generated.resources.cancel
import qmessenger.composeapp.generated.resources.decentralized_channel
import qmessenger.composeapp.generated.resources.edit_channel_description
import qmessenger.composeapp.generated.resources.edit_channel_description_placeholder
import qmessenger.composeapp.generated.resources.edit_channel_description_title
import qmessenger.composeapp.generated.resources.edit_channel_title
import qmessenger.composeapp.generated.resources.edit_channel_title_placeholder
import qmessenger.composeapp.generated.resources.edit_channel_title_title
import qmessenger.composeapp.generated.resources.internal_error
import qmessenger.composeapp.generated.resources.member_count
import qmessenger.composeapp.generated.resources.save

@Composable
fun ChannelConfig(channel: Channel) {
    val scope = rememberCoroutineScope()
    var channelConf by remember { mutableStateOf<ChannelConfInfo?>(null) }
    var nickname by remember { mutableStateOf(TextFieldValue("")) }
    val scrollState = rememberScrollState()
    val imageLoader =
        ImageLoader.Builder(LocalPlatformContext.current).diskCachePolicy(CachePolicy.DISABLED)
            .memoryCachePolicy(CachePolicy.ENABLED).build()

    LaunchedEffect(channel) {
        // Fetch channel configuration
        val conf = QMessenger.channelConf(channel.id)
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

    var editDescriptionDialog by remember { mutableStateOf(false) }
    var editTitleDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.verticalScroll(scrollState)) {
        Row {
            AsyncImage(
                modifier = Modifier.size(65.dp, 65.dp).clip(CircleShape),
                model = "${config.api}/api/avatar/image/${channel.name}",
                contentDescription = "Avatar of channel ${channel.name}",
                imageLoader = imageLoader
            )

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    modifier = Modifier.clickable {
                        editTitleDialog = true
                    },
                    text = channel.title ?: channel.name
                )
                Text(
                    text = stringResource(Res.string.member_count).replace(
                        "*count*",
                        channel.memberCount.toString()
                    ), color = MaterialTheme.colorScheme.onBackground.copy(0.5f)
                )
            }
        }

        Row {
            var visible by remember { mutableStateOf(channel.publicChannel) }
            AssistChip(
                onClick = {
                    scope.launch {
                        QMessenger.updateChannelVisible(channel.id, !channel.publicChannel).let {
                            if (it.isSuccess) {
                                val response = it.getOrThrow()
                                if (response.code == 200) {
                                    val state = response.data!!.visible
                                    channel.publicChannel = state
                                    visible = state
                                }
                            }
                        }
                    }
                },
                label = { Text(if (visible) "Public" else "Private") },
                leadingIcon = {
                    Icon(
                        if (visible) Icons.Filled.Public else Icons.Filled.Lock,
                        contentDescription = "Channel visible",
                        Modifier.size(AssistChipDefaults.IconSize)
                    )
                }
            )

            Spacer(modifier = Modifier.padding(5.dp))

            if (channel.decentralized) {
                AssistChip(
                    onClick = {
                    },
                    label = { Text(stringResource(Res.string.decentralized_channel)) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.RemoveModerator,
                            contentDescription = "Decentralized",
                            Modifier.size(AssistChipDefaults.IconSize)
                        )
                    }
                )
            }
        }

        SelectionContainer {
            val wrappedText = buildString {
                channel.description.chunked(50).forEach { chunk ->
                    append(chunk)
                    append('\n') // Insert a newline character
                }
            }
            Text(
                text = wrappedText,
                modifier = Modifier.clickable {
                    if (channelConf?.permissions?.contains(Permission.MANAGE_CHANNEL) == true) {
                        editDescriptionDialog = true
                    }
                }
            )
        }

        if (editDescriptionDialog) {
            EditDescriptionDialog(channel) { newDescription ->
                newDescription?.let {
                    channel.description = it
                }
                editDescriptionDialog = false
            }
        }

        if (editTitleDialog) {
            EditTitleDialog(channel) { newTitle ->
                newTitle?.let {
                    channel.title = it
                }
                editTitleDialog = false
            }
        }


        if (channel.description.isBlank() && channelConf?.permissions?.contains(Permission.MANAGE_CHANNEL) == true) {
            Button(
                onClick = {
                    editDescriptionDialog = true
                }
            ) {
                Text(stringResource(Res.string.edit_channel_description))
            }
        }
    }
}

@Composable
fun EditDescriptionDialog(channel: Channel, onDismissRequest: (String?) -> Unit) {
    var description by remember { mutableStateOf(channel.description) }
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf("") }

    // Dialog content
    AlertDialog(
        onDismissRequest = { onDismissRequest(null) },
        title = { Text(text = stringResource(Res.string.edit_channel_description)) },
        text = {
            Column {
                Text(
                    text = stringResource(Res.string.edit_channel_description_title),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text(text = stringResource(Res.string.edit_channel_description_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text
                    )
                )
                if (error.isNotEmpty()) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        QMessenger.updateChannelDescription(channel, description).let {
                            if (it.isSuccess) {
                                val response = it.getOrThrow()
                                if (response.code == 200) {
                                    onDismissRequest(response.data!!.description)
                                } else {
                                    error = response.message
                                }
                            } else {
                                error = getString(Res.string.internal_error)
                            }
                        }
                    }
                }
            ) {
                Text(stringResource(Res.string.save))
            }
        },
        dismissButton = {
            Button(onClick = { onDismissRequest(null) }) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}

@Composable
fun EditTitleDialog(channel: Channel, onDismissRequest: (String?) -> Unit) {
    var title by remember { mutableStateOf(channel.title?: channel.name) }
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf("") }

    // Dialog content
    AlertDialog(
        onDismissRequest = { onDismissRequest(null) },
        title = { Text(text = stringResource(Res.string.edit_channel_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(Res.string.edit_channel_title_title),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text(text = stringResource(Res.string.edit_channel_title_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text
                    )
                )
                if (error.isNotEmpty()) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        QMessenger.updateChannelTitle(channel.id, title).let {
                            if (it.isSuccess) {
                                val response = it.getOrThrow()
                                if (response.code == 200) {
                                    onDismissRequest(response.data!!.title)
                                } else {
                                    error = response.message
                                }
                            } else {
                                error = getString(Res.string.internal_error)
                            }
                        }
                    }
                }
            ) {
                Text(stringResource(Res.string.save))
            }
        },
        dismissButton = {
            Button(onClick = { onDismissRequest(null) }) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}