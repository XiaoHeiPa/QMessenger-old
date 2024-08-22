package org.cubewhy.chat

import kotlinx.serialization.Serializable

// https://github.com/cubewhy/QMsgBackend/tree/master/src/main/java/org/cubewhy/chat/entity

@Serializable
data class RestBean<T>(
    val code: Int,
    val data: T?,
    val message: String
)

@Serializable
data class Authorize(
    val username: String,
    val token: String,
    val email: String,
    val roles: Set<Role>,
    val expire: Long
)

@Serializable
data class Role(
    val id: Long,
    val name: String,
    val description: String,
    val permissions: Set<Permission>
)

enum class Permission(val type: Type) {
    // servlet admin rights
    DASHBOARD(Type.SERVLET), // 访问后端的仪表盘
    MANAGE_USER(Type.SERVLET), // 管理所有用户
    MANAGE_ROLES(Type.CHANNEL_AND_SERVLET), // 管理身份组
    MANAGE_FILES(Type.CHANNEL_AND_SERVLET), // 管理用户上传的文件
    REGISTER_INVITE(Type.SERVLET), // 生成注册邀请

    // servlet admin & channel admin rights
    MANAGE_CHANNEL(Type.CHANNEL_AND_SERVLET), // 管理频道
    DISBAND_CHANNEL(Type.CHANNEL_AND_SERVLET), // 解散频道
    KICK_USERS(Type.CHANNEL_AND_SERVLET), // 频道内为踢出成员,服务器内为注销账户

    // channel admin rights
    PROCESS_JOIN_REQUEST(Type.CHANNEL), // 处理加频道请求

    // user permissions
    SEND_MESSAGE(Type.CHANNEL_AND_SERVLET), // 发送消息
    CREATE_CHANNEL(Type.SERVLET), // 创建频道
    JOIN_CHANNEL(Type.SERVLET), // 加入频道
    VIEW_CHANNEL(Type.CHANNEL), // 查看消息
    SEND_CHANNEL_INVITE(Type.CHANNEL), // 发送加频道邀请
    UPLOAD_FILES(Type.CHANNEL_AND_SERVLET), // 上传文件
    DOWNLOAD_FILES(Type.CHANNEL_AND_SERVLET); // 下载文件

    enum class Type {
        CHANNEL, // 群组权限
        SERVLET, // (全局) 服务器权限
        CHANNEL_AND_SERVLET // 重合
    }
}

@Serializable
data class RegisterInfo(
    val username: String,
    val password: String,
    val email: String,

    val nickname: String,
    val bio: String,

    val inviteCode: String?
)

@Serializable
data class Account(
    val id: Long,
    val username: String,
    val nickname: String,
    val avatarHash: String?,
    val email: String,
    val bio: String?,

    val registerTime: Long,
    val updatedTime: Long,
    val roles: List<String>
)

@Serializable
data class UpdateFirebaseToken(
    val token: String
)

@Serializable
data class WebsocketResponse<T>(
    val method: String,
    val data: T?
) {
    companion object {
        const val NEW_MESSAGE: String = "nmsg"
    }
}

@Serializable
data class Channel(
    val id: Long,

    val name: String,
    val title: String?,
    val description: String,

    val iconHash: String?,
    val publicChannel: String,
    val decentralized: String,

    val createdAt: Long,
    val memberCount: Long
)

@Serializable
data class ChatMessage<T>(
    val id: Long,
    val channel: Long,
    val sender: Long,
    val contentType: String,
    val shortContent: String,
    val content: T,
    val timestamp: Long,
)

@Serializable
data class BaseMessage(
    val data: String
)