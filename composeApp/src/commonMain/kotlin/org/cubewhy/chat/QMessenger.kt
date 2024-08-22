package org.cubewhy.chat

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.send
import kotlinx.serialization.builtins.serializer

val client = getHttpClient {
    install(ContentNegotiation) {
        json()
    }

    install(WebSockets)
}

object QMessenger {
    private var session: DefaultClientWebSocketSession? = null

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

    suspend fun websocket(): WebSocketSession? {
        if (session == null) {
            session = runCatching {
                client.webSocketSession(config.websocket)
            }.let {
                if (it.isSuccess) it.getOrThrow()
                else null
            }
        }
        return session
    }

    suspend fun channels(): Result<List<Channel>> = runCatching {
        val response: RestBean<List<Channel>> = client.get("${config.api}/api/channel/list") {
            header("Authorization", "Bearer ${config.user!!.token}")
        }.body()
        if (response.code != 200) {
            throw IllegalStateException(response.message)
        }
        response.data!!
    }

    suspend fun user(token: String): Result<Account> = runCatching {
        val response: RestBean<Account> = client.get("${config.api}/api/user/whoami") {
            header("Authorization", "Bearer ${config.user!!.token}")
        }.body()
        if (response.code != 200) {
            throw IllegalStateException(response.message)
        }
        response.data!!
    }

    suspend fun sendMessage(text: String, channel: Channel, user: Account) {
        val preview = text.split("\n")[0]
        val message = ChatMessageDTO(channel.id, "${user.nickname}: $preview", MessageType.TEXT, text)
        this.websocket()?.send(
            JSON.encodeToString(
                WebsocketRequest.serializer(ChatMessageDTO.serializer(String.serializer())),
                WebsocketRequest(WebsocketRequest.SEND_MESSAGE, message)
            )
        )
    }

    fun channel(id: Long): Channel {
        TODO("Not yet implemented")
    }
}

expect fun pushNotification(title: String, body: String): Unit