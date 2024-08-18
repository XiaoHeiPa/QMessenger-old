package org.cubewhy.chat

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

val configFile = getAppConfigDirectory().resolve("config.json").toPath()

actual fun loadConfig(): AppConfig {
    if (Files.exists(configFile)) {
        val json = Files.readString(configFile)
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
    Files.writeString(configFile, json)
}