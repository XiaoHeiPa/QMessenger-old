package org.cubewhy.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.launch
import org.cubewhy.chat.theme.QMessengerTheme
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import qmessenger.composeapp.generated.resources.Res
import qmessenger.composeapp.generated.resources.app_name
import qmessenger.composeapp.generated.resources.empty_username_or_password
import qmessenger.composeapp.generated.resources.encrypted_connection
import qmessenger.composeapp.generated.resources.internal_error
import qmessenger.composeapp.generated.resources.invite_code
import qmessenger.composeapp.generated.resources.login
import qmessenger.composeapp.generated.resources.password
import qmessenger.composeapp.generated.resources.register
import qmessenger.composeapp.generated.resources.register_tip
import qmessenger.composeapp.generated.resources.server
import qmessenger.composeapp.generated.resources.server_address
import qmessenger.composeapp.generated.resources.server_confirm
import qmessenger.composeapp.generated.resources.username

val config = loadConfig()
val client = getHttpClient {
    install(ContentNegotiation) {
        json()
    }
}

private fun checkLogin(): Boolean {
    if (config.user == null) return false
    if (config.user!!.expireAt < getTimeMillis()) return false
    return true
}

@Composable
@Preview
fun App() {
    QMessengerTheme {
        Scaffold { inn ->
            val scope = rememberCoroutineScope()

            var startDestination by remember { mutableStateOf(Screen.LOGIN_FORM) }
            if (checkLogin()) {
                startDestination = Screen.CHAT
            } else if (config.user != null) {
                // flash token
                scope.launch {
                    config.user?.let {
                        QMessenger.login(config.user!!.username, decrypt(config.user!!.password))
                            .let {
                                if (it.isSuccess) {
                                    it.getOrThrow().let { response ->
                                        if (response.code == 200) {
                                            config.user!!.token = response.data!!.token
                                            saveConfig(config)
                                            startDestination = Screen.CHAT
                                        }
                                    }
                                }
                            }
                    }
                }
            }


            val navController = rememberNavController()
            Column(
                Modifier.padding(inn).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NavHost(navController, startDestination = startDestination) {
                    composable(route = Screen.LOGIN_FORM) {
                        LoginForm {
                            navController.navigate(Screen.CHAT)
                        }
                    }

                    composable(route = Screen.CHAT) {
                        ChatScreen(navController)
                    }
                }
            }
        }
    }
}

object Screen {
    const val CHAT = "chat"
    const val LOGIN_FORM = "login-form"
}

@Composable
fun RegisterDialog(
    onDismiss: () -> Unit = {}, onRegisterSuccess: (String, String) -> Unit
) = Dialog(
    onDismissRequest = { onDismiss() }, properties = DialogProperties(
        dismissOnBackPress = true, dismissOnClickOutside = true
    )
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    var hasError by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Register", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(stringResource(Res.string.username)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(Res.string.password)) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle Password Visibility"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Nickname") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Bio") },
            modifier = Modifier.fillMaxWidth()
                .heightIn(min = 100.dp) // Set a minimum height for the bio field
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = inviteCode,
            onValueChange = { inviteCode = it },
            label = { Text(stringResource(Res.string.invite_code)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (hasError) {
            Text(text = error, color = Color.Red)
            Spacer(modifier = Modifier.height(5.dp))
        }

        Button(
            onClick = {
                val info = RegisterInfo(
                    username = username,
                    password = password,
                    email = email,
                    nickname = nickname,
                    bio = bio,
                    inviteCode = inviteCode.ifBlank { null }
                )
                scope.launch {
                    runCatching {
                        val response: RestBean<Account> =
                            client.post("${config.api}/api/user/register") {
                                setBody(info)
                                contentType(ContentType.parse("application/json"))
                            }.body()
                        response
                    }.let {
                        if (it.isSuccess) {
                            it.getOrThrow().let { response ->
                                if (response.code == 200) {
                                    onDismiss()
                                    onRegisterSuccess(username, password)
                                } else {
                                    hasError = true
                                    error = response.message
                                }
                            }
                        } else {
                            hasError = true
                            error = getString(Res.string.internal_error)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.register))
        }
    }
}

@Composable
fun LoginForm(modifier: Modifier = Modifier, onSuccess: (Authorize) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var serverUrl by remember { mutableStateOf(config.serverUrl) }

    var showSwitchServerDialog by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    val shakeAnim = remember {
        Animatable(0f)
    }

    val haptic = LocalHapticFeedback.current

    Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = {
        SnackbarHost(hostState = snackBarHostState)
    }, floatingActionButton = {
        AnimatedVisibility(
            visible = !showSwitchServerDialog,
            enter = slideInVertically(),
            exit = slideOutVertically()
        ) {
            FloatingActionButton(shape = CircleShape, onClick = {
                showSwitchServerDialog = true
            }) {
                Icon(
                    imageVector = Icons.Filled.Edit, contentDescription = "Edit"
                )
            }
        }
    }) { inn ->
        Box(
            modifier = Modifier.fillMaxSize().padding(inn)
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

                TextField(value = username, label = {
                    Text(stringResource(Res.string.username))
                }, onValueChange = {
                    username = it
                }, modifier = Modifier.fillMaxWidth().padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = password,
                    label = {
                        Text(stringResource(Res.string.password))
                    },
                    onValueChange = {
                        password = it
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                )
                if (hasError) {
                    Text(text = error, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(modifier = Modifier.offset(x = shakeAnim.value.dp), onClick = {
                    scope.launch {
                        if (username.isEmpty() || password.isEmpty()) {
                            hasError = true
                            error = getString(Res.string.empty_username_or_password)
                            shake(shakeAnim, haptic)
                        } else {
                            // Perform login
                            hasError = false
                            QMessenger.login(username, password).let { result ->
                                if (result.isSuccess) {
                                    val response = result.getOrThrow()
                                    if (response.code == 200) {
                                        config.user = UserCache(
                                            username = username,
                                            password = encrypt(password),
                                            token = response.data!!.token,
                                            expireAt = response.data.expire
                                        )
                                        saveConfig(config)
                                        onSuccess(response.data)
                                    } else {
                                        shake(shakeAnim, haptic)
                                        error = response.message
                                        hasError = true
                                    }
                                }
                                if (result.isFailure) {
                                    hasError = true
                                    error = getString(Res.string.internal_error)
                                    shake(shakeAnim, haptic)
                                }
                            }
                        }
                    }
                }) {
                    Text(text = stringResource(Res.string.login))
                }
            }

            Box(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
            ) {
                Text(
                    text = serverUrl, fontSize = 16.sp, color = Color.White
                )
            }

            Box(
                modifier = Modifier.align(Alignment.BottomCenter).padding(35.dp)
            ) {
                Text(
                    modifier = Modifier.clickable { showRegisterDialog = true },
                    text = stringResource(Res.string.register_tip)
                )
            }
        }

        if (showSwitchServerDialog) {
            SwitchServerDialog {
                showSwitchServerDialog = false
                serverUrl = config.serverUrl
            }
        }

        if (showRegisterDialog) {
            RegisterDialog(onDismiss = { showRegisterDialog = false }) { username1, password1 ->
                // 自动输入账户名密码
                username = username1
                password = password1
            }
        }
    }
}

private suspend fun shake(
    shakeAnim: Animatable<Float, AnimationVector1D>, haptic: HapticFeedback
) {
    shakeAnim.animateTo(
        targetValue = 10f, animationSpec = repeatable(
            iterations = 5, animation = tween(
                durationMillis = 100, easing = LinearEasing
            )
        ), initialVelocity = 0f
    )
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    shakeAnim.snapTo(0f)
}

@Composable
fun SwitchServerDialog(onDismiss: () -> Unit) = Dialog(
    onDismissRequest = { onDismiss() }, properties = DialogProperties(
        dismissOnBackPress = true, dismissOnClickOutside = true
    )
) {
    var serverUrl by remember { mutableStateOf(config.serverUrl) }
    var encrypted by remember { mutableStateOf(config.encryptedConnection) }
    Card(
        modifier = Modifier.padding(16.dp)

    ) {
        Column(
            modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(Res.string.server), fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            TextField(value = serverUrl, onValueChange = {
                serverUrl = it
            }, placeholder = {
                Text(text = stringResource(Res.string.server_address))
            })
            Row(
                modifier = Modifier.padding(vertical = 15.dp)
            ) {
                IconButton(onClick = {
                    encrypted = !encrypted
                }) {
                    Icon(
                        imageVector = if (encrypted) Icons.Filled.Lock else Icons.Filled.LockOpen,
                        contentDescription = stringResource(Res.string.encrypted_connection)
                    )
                }
                Button(onClick = {
                    config.serverUrl = serverUrl
                    config.encryptedConnection = encrypted
                    saveConfig(config)
                    onDismiss()
                }) {
                    Text(stringResource(Res.string.server_confirm))
                }
            }
        }
    }
}