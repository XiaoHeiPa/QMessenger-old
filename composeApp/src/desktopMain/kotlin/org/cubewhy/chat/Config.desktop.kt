package org.cubewhy.chat

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

actual fun loadConfig(): AppConfig {
    val path = Paths.get("config.json")
    if (Files.exists(path)) {
        val json = Files.readString(path)
        return JSON.decodeFromString(json)
    }
    return AppConfig()
}

 fun getAppConfigDirectory(): File {
    val userHome = System.getProperty("user.home")
    return File(userHome).resolve(".cubewhy").resolve("messenger").absoluteFile.also {
        it.mkdirs()
    }
}

actual fun saveConfigToStorage(json: String) {
    Files.writeString(getAppConfigDirectory().resolve("config.json").toPath(), json)
}