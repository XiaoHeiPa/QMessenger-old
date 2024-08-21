package org.cubewhy.chat

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
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

    suspend fun fcm(token: String) = runCatching {
        val response: RestBean<String> =
            client.post("${config.api}/api/push/fcm") {
                setBody(UpdateFirebaseToken(token = token))
            }.body()
        response
    }
}