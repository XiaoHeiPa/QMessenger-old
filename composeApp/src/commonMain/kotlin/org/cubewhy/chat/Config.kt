package org.cubewhy.chat

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

val JSON = Json { ignoreUnknownKeys = true }

@Serializable
data class AppConfig(
    var serverUrl: String = "chat-api.lunarclient.top",
    var encryptedConnection: Boolean = true,
    var fold: Boolean = true, // fold conversations

    var user: UserCache? = null
) {
    val api: String
        get() =
            (if (encryptedConnection) "https://" else "http://") + serverUrl
    val websocket: String
        get() = (if (encryptedConnection) "wss://" else "ws://") + serverUrl + "/websocket"
}

@Serializable
data class UserCache(
    var username: String,
    var password: String,
    var token: String,
    var expireAt: Long
)
// todo oauth

@OptIn(ExperimentalEncodingApi::class)
fun decrypt(input: String): String =
    Base64.decode(input.encodeToByteArray()).decodeToString()

@OptIn(ExperimentalEncodingApi::class)
fun encrypt(input: String): String =
    Base64.encode(input.encodeToByteArray())

expect fun loadConfig(): AppConfig

fun saveConfig(config: AppConfig) {
    val json = JSON.encodeToString(AppConfig.serializer(), config)
    saveConfigToStorage(json)
}

expect fun saveConfigToStorage(json: String)