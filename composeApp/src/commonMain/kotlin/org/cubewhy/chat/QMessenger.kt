package org.cubewhy.chat

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

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

    suspend fun websocket(handleMessage: (ChatMessage<BaseMessage>) -> Unit) = runCatching {
        client.webSocket(config.websocket) {
            for (message in incoming) {
                when (message) {
                    is Frame.Text -> {
                        val response: WebsocketResponse<JsonObject> = JSON.decodeFromString(message.readText())
                        if (response.method == WebsocketResponse.NEW_MESSAGE) {
                            val msg: ChatMessage<BaseMessage> = JSON.decodeFromJsonElement(response.data!!)
                            handleMessage(msg)
                        }
                    }

                    else -> {
                        // unknown type
                    }
                }
            }
        }
    }
}

expect fun pushNotification(title: String, body: String): Unit