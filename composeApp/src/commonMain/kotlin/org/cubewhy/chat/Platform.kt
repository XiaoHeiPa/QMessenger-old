package org.cubewhy.chat

import androidx.compose.runtime.Composable
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

interface Platform {
    val name: String
    val type: PlatformType
}

enum class PlatformType {
    DESKTOP,
    ANDROID,
    WEB
}

expect fun getPlatform(): Platform

expect fun getHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient
