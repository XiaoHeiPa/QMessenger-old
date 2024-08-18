package org.cubewhy.chat

import kotlinx.browser.window

actual fun loadConfig(): AppConfig {
    val json = window.localStorage.getItem("app_config") ?: return AppConfig()
    return JSON.decodeFromString(json)
}

actual fun saveConfigToStorage(json: String) {
    window.localStorage.setItem("app_config", json)
}