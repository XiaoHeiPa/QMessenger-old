package org.cubewhy.chat

import androidx.compose.runtime.Composable
import kotlinx.browser.window

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

@Composable
actual fun isDarkEnabled(): Boolean = window.matchMedia("(prefers-color-scheme: dark)").matches