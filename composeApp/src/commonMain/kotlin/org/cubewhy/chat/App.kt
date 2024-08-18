package org.cubewhy.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.launch
import org.cubewhy.chat.theme.QMessengerTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import qmessenger.composeapp.generated.resources.Res
import qmessenger.composeapp.generated.resources.*

val config = loadConfig()

@Composable
@Preview
fun App() {
    QMessengerTheme {
        Scaffold {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                LoginForm()
            }
        }
    }
}

@Composable
fun LoginForm(modifier: Modifier = Modifier) {
//    val client = HttpClient(CIO) {
//        install(JsonFeature) {
//            serializer = KotlinxSerializer() // JSON serialization
//        }
//    }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    var serverUrl by remember { mutableStateOf(config.serverUrl) }

    var showSwitchServerDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val shakeAnim = remember {
        Animatable(0f)
    }

    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !showSwitchServerDialog,
                enter = slideInVertically(),
                exit = slideOutVertically()
            ) {
                FloatingActionButton(
                    shape = CircleShape,
                    onClick = {
                        showSwitchServerDialog = true
                    }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
        }
    ) { inn ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inn)
                .padding(16.dp) // To add padding around the Box
        ) {
            Column(
                modifier = Modifier.padding(30.dp).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.app_name),
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = username,
                    placeholder = {
                        Text(stringResource(Res.string.username))
                    },
                    onValueChange = {
                        username = it
                    },
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = password,
                    placeholder = {
                        Text(stringResource(Res.string.password))
                    },
                    onValueChange = {
                        password = it
                        passwordError = password.length < 6
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                )
                if (passwordError) {
                    Text(text = stringResource(Res.string.password_length), color = Color.Red)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    modifier = Modifier.offset(x = shakeAnim.value.dp),
                    onClick = {
                        scope.launch {
                            if (passwordError || username.isEmpty() || password.isEmpty()) {
                                shakeAnim.animateTo(
                                    targetValue = 10f,
                                    animationSpec = repeatable(
                                        iterations = 5,
                                        animation = tween(
                                            durationMillis = 100,
                                            easing = LinearEasing
                                        )
                                    ),
                                    initialVelocity = 0f
                                )
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                shakeAnim.snapTo(0f)
                            } else {
                                // Perform login
//                                val response: HttpResponse = client.post("http://backend.lexap.top/api/user/login") {
//                                    contentType(ContentType.Application.Json)
//                                }
                            }
                        }
                    }) {
                    Text(text = stringResource(Res.string.login))
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = serverUrl,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }

        if (showSwitchServerDialog) {
            SwitchServerDialog {
                showSwitchServerDialog = false
                serverUrl = config.serverUrl
            }
        }
    }
}

@Composable
fun SwitchServerDialog(onDismiss: () -> Unit) = Dialog(
    onDismissRequest = { onDismiss() },
    properties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = true
    )
) {
    var serverUrl by remember { mutableStateOf(config.serverUrl) }
    Box(
        modifier = Modifier
            .padding(16.dp)

    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(Res.string.server), fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = serverUrl,
                onValueChange = {
                    serverUrl = it
                },
                placeholder = {
                    Text(text = stringResource(Res.string.server_address))
                }
            )
            Button(onClick = {
                config.serverUrl = serverUrl
                saveConfig(config)
                onDismiss()
            }) {
                Text(stringResource(Res.string.server_confirm))
            }
        }
    }
}