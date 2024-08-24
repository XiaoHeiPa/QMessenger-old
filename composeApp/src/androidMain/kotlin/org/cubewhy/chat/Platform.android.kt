package org.cubewhy.chat

import android.os.Build
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val type: PlatformType = PlatformType.ANDROID
}

actual fun getPlatform(): Platform = AndroidPlatform()
actual fun getHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(CIO, config)