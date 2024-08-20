package org.cubewhy.chat

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun getHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(Js, config)