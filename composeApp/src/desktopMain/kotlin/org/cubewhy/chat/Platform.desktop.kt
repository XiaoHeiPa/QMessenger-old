package org.cubewhy.chat

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO

actual fun getHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(CIO, config)