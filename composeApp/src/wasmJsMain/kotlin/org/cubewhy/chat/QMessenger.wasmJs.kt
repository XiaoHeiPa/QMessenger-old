package org.cubewhy.chat

import org.w3c.notifications.Notification
import org.w3c.notifications.NotificationOptions

actual fun pushNotification(title: String, body: String) {
    var n = Notification(title, NotificationOptions(body = body))
}