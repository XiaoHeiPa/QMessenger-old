package org.cubewhy.chat

import androidx.preference.PreferenceManager

data class ConfigHelper(
    val activity: MainActivity? = null
)

var helper = ConfigHelper()

actual fun loadConfig(): AppConfig {
    val prefs = PreferenceManager.getDefaultSharedPreferences(helper.activity!!.baseContext)
    val json = prefs.getString("app_config", "{}") ?: return AppConfig()
    return JSON.decodeFromString(json)
}

actual fun saveConfigToStorage(json: String) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(helper.activity!!.baseContext)
    prefs.edit().putString("app_config", json).apply()
}
