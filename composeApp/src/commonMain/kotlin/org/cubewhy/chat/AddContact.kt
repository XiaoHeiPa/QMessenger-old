package org.cubewhy.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import qmessenger.composeapp.generated.resources.Res
import qmessenger.composeapp.generated.resources.channel_description
import qmessenger.composeapp.generated.resources.channel_invite_code
import qmessenger.composeapp.generated.resources.channel_name
import qmessenger.composeapp.generated.resources.channel_title
import qmessenger.composeapp.generated.resources.create_channel
import qmessenger.composeapp.generated.resources.decentralized_channel
import qmessenger.composeapp.generated.resources.internal_error
import qmessenger.composeapp.generated.resources.join_channel
import qmessenger.composeapp.generated.resources.public_channel
import qmessenger.composeapp.generated.resources.switch_to_create_channel
import qmessenger.composeapp.generated.resources.switch_to_join_channel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AddContact(modifier: Modifier = Modifier, onSuccess: () -> Unit) {
    val scope = rememberCoroutineScope()

    var inviteCode by remember { mutableStateOf("") }
    var channelName by remember { mutableStateOf("") }
    var channelDescription by remember { mutableStateOf("") }
    var channelTitle by remember { mutableStateOf("") }
    var isCreatingChannel by remember { mutableStateOf(true) }

    var isPublicChannel by remember { mutableStateOf(false) }
    var isDecentralizedChannel by remember { mutableStateOf(false) }

    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isCreatingChannel) "Create a Channel" else "Join a Channel",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // AnimatedContent for transition between views
        AnimatedContent(
            targetState = isCreatingChannel,
            transitionSpec = {
                // Transition animation
                if (targetState) {
                    slideInVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) with
                            slideOutVertically(animationSpec = tween(300)) + fadeOut(
                        animationSpec = tween(
                            300
                        )
                    )
                } else {
                    slideInVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)) with
                            slideOutVertically(animationSpec = tween(300)) + fadeOut(
                        animationSpec = tween(
                            300
                        )
                    )
                }.using(
                    SizeTransform(clip = false)
                )
            }
        ) { targetState ->
            if (targetState) {
                // Create Channel
                Column {
                    TextField(
                        value = channelName,
                        onValueChange = { channelName = it },
                        label = { Text(stringResource(Res.string.channel_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = channelTitle,
                        onValueChange = { channelTitle = it },
                        label = { Text(stringResource(Res.string.channel_title)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = channelDescription,
                        onValueChange = { channelDescription = it },
                        label = { Text(stringResource(Res.string.channel_description)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { isPublicChannel = !isPublicChannel }
                    ) {
                        Checkbox(
                            checked = isPublicChannel,
                            onCheckedChange = { isPublicChannel = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.public_channel))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { isDecentralizedChannel = !isDecentralizedChannel }
                    ) {
                        Checkbox(
                            checked = isDecentralizedChannel,
                            onCheckedChange = { isDecentralizedChannel = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.decentralized_channel))
                    }
                }
            } else {
                // Join Channel
                TextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it },
                    label = { Text(stringResource(Res.string.channel_invite_code)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(hasError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error, // Use red color for error
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                scope.launch {
                    if (isCreatingChannel) {
                        QMessenger.createChannel(channelName, channelDescription, channelTitle, isPublicChannel, isDecentralizedChannel).let {
                            if (it.isSuccess) {
                                val (code, _, message) = it.getOrThrow()
                                if (code == 200) {
                                    onSuccess()
                                } else {
                                    hasError = true
                                    errorMessage = message
                                }
                            } else {
                                hasError = true
                                errorMessage = getString(Res.string.internal_error)
                                it.exceptionOrNull()?.let { it1 -> println(it1) }
                            }
                        }
                    } else {
                        QMessenger.joinChannel(inviteCode).let {
                            if (it.isSuccess) {
                                val (code, _, message) = it.getOrThrow()
                                if (code == 200) {
                                    onSuccess()
                                } else {
                                    hasError = true
                                    errorMessage = message
                                }
                            } else {
                                hasError = true
                                errorMessage = getString(Res.string.internal_error)
                                it.exceptionOrNull()?.let { it1 -> println(it1) }
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(if (isCreatingChannel) Res.string.create_channel else Res.string.join_channel))
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { isCreatingChannel = !isCreatingChannel }
        ) {
            Text(text = stringResource(if (isCreatingChannel) Res.string.switch_to_join_channel else Res.string.switch_to_create_channel))
        }
    }
}