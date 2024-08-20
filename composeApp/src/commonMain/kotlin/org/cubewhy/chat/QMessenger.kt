package org.cubewhy.chat

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.serialization.kotlinx.json.json

val client = getHttpClient {
    install(ContentNegotiation) {
        json()
    }
}

object QMessenger {
    suspend fun login(username: String, password: String) = runCatching {
        val response: RestBean<Authorize> =
            client.post("${config.api}/api/user/login?username=$username&password=$password")
                .body()
        response
    }
}