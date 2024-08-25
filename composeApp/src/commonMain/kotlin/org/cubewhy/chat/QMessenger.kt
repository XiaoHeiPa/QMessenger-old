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
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.send
import kotlinx.serialization.json.encodeToJsonElement

val client = getHttpClient {
    install(ContentNegotiation) {
        json(json = JSON)
    }
    headers {
        set("User-Agent", "qmsg-client")
    }

    install(WebSockets) {
        pingInterval = 5_000

    }
}

object QMessenger {
    private var channel: Channel? = null
    private var session: DefaultClientWebSocketSession? = null

    suspend fun check() = runCatching {
        val response: RestBean<CheckStatus> =
            client.get("${config.api}/api/status/check")
                .body()
        response
    }

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

    suspend fun websocket(user: Account, channel: Channel?): WebSocketSession? {
        if (this.channel == null || channel?.id != this.channel?.id) {
            this.channel = channel
        }
        if (session == null) {
            session = runCatching {
                client.webSocketSession(config.websocket) {
                    header("Authorization", "Bearer ${config.user!!.token}")
                }
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

    suspend fun user(): Result<RestBean<Account>> = runCatching {
        val response: RestBean<Account> = client.get("${config.api}/api/user/whoami") {
            header("Authorization", "Bearer ${config.user!!.token}")
        }.body()
        response
    }

    suspend fun sendMessage(text: String, channel: Channel, user: Account) = runCatching {
        val preview = text.split("\n")[0]
        val message = ChatMessageDTO(
            channel = channel.id,
            shortContent = "${user.nickname}: $preview",
            content = listOf(
                JSON.encodeToJsonElement(
                    TextMessage(
                        data = text,
                        type = MessageType.TEXT
                    )
                )
            )
        )
        // TODO parse links
        this.websocket(user, channel)!!.send(
            JSON.encodeToString(
                WebsocketRequest.serializer(ChatMessageDTO.serializer()),
                WebsocketRequest(WebsocketRequest.SEND_MESSAGE, message)
            )
        )
    }

    suspend fun channelConf(id: Long) = runCatching {
        val response: RestBean<ChannelConfInfo> =
            client.get("${config.api}/api/channel/${id}/myInfo") {
                header("Authorization", "Bearer ${config.user!!.token}")
            }.body()
        response
    }

    suspend fun channelInfo(id: Long) = runCatching {
        val response: RestBean<Channel> =
            client.get("${config.api}/api/channel/${id}/info") {
                header("Authorization", "Bearer ${config.user!!.token}")
            }.body()
        response
    }

    suspend fun createChannel(
        name: String,
        description: String,
        title: String,
        isPublic: Boolean,
        decentralized: Boolean
    ) = runCatching {
        val response: RestBean<Channel> = client.post("${config.api}/api/channel/create") {
            header("Authorization", "Bearer ${config.user!!.token}")
            contentType(ContentType.parse("application/json"))
            setBody(
                ChannelDTO(
                    name = name,
                    title = title,
                    description = description,
                    publicChannel = isPublic,
                    decentralized = decentralized
                )
            )
        }.body()
        response
    }

    suspend fun updateChannelDescription(channel: Channel, description: String) = runCatching {
        val response: RestBean<UpdateChannelDescription> = client.post("${config.api}/api/channel/${channel.id}/description") {
            header("Authorization", "Bearer ${config.user!!.token}")
            contentType(ContentType.parse("application/json"))
            setBody(
                UpdateChannelDescription(description)
            )
        }.body()
        response
    }

    suspend fun joinChannel(inviteCode: String) = runCatching {
        val response: RestBean<String> =
            client.post("${config.api}/api/channel/invite/${inviteCode}/use") {
                header("Authorization", "Bearer ${config.user!!.token}")
            }.body()
        response
    }

    suspend fun messages(channel: Channel, page: Int) = runCatching {
        val response: RestBean<List<ChatMessage>> =
            client.get("${config.api}/api/channel/messages?channel=${channel.id}&page=$page&size=100") {
                header("Authorization", "Bearer ${config.user!!.token}")
            }.body()
        response
    }

    suspend fun updateChannelNickname(channelId: Long, text: String) = runCatching {
        val response: RestBean<UpdateChannelNickname> =
            client.post("${config.api}/api/channel/$channelId/nickname") {
                header("Authorization", "Bearer ${config.user!!.token}")
                contentType(ContentType.parse("application/json"))
                setBody(UpdateChannelNickname(nickname = text))
            }.body()
        response
    }

    suspend fun updateChannelVisible(channelId: Long, state: Boolean) = runCatching {
        val response: RestBean<UpdateChannelVisible> =
            client.post("${config.api}/api/channel/$channelId/visible") {
                header("Authorization", "Bearer ${config.user!!.token}")
                contentType(ContentType.parse("application/json"))
                setBody(UpdateChannelVisible(visible = state))
            }.body()
        response
    }

    suspend fun updateChannelTitle(channelId: Long, title: String) = runCatching {
        val response: RestBean<UpdateChannelTitle> =
            client.post("${config.api}/api/channel/$channelId/title") {
                header("Authorization", "Bearer ${config.user!!.token}")
                contentType(ContentType.parse("application/json"))
                setBody(UpdateChannelTitle(title = title))
            }.body()
        response
    }
}

expect fun pushNotification(title: String, body: String): Unit