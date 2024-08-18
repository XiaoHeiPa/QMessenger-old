package org.cubewhy.chat

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val JSON = Json { ignoreUnknownKeys = true }

@Serializable
data class AppConfig(
    var serverUrl: String = "chat.lunarclient.top"
)

expect fun loadConfig(): AppConfig

fun saveConfig(config: AppConfig) {
    val json = JSON.encodeToString(AppConfig.serializer(), config)
    saveConfigToStorage(json)
}

expect fun saveConfigToStorage(json: String)