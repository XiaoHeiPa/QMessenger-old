package org.cubewhy.chat

import io.ktor.client.call.body
import io.ktor.client.request.post

object QMessenger {
    suspend fun login(username: String, password: String) = runCatching {
        val response: RestBean<Authorize> =
            client.post("${config.api}/api/user/login?username=$username&password=$password")
                .body()
        response
    }
}