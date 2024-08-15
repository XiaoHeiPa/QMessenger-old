package org.cubewhy.chat

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform