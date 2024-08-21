package org.cubewhy.chat

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.w3c.notifications.DENIED
import org.w3c.notifications.Notification
import org.w3c.notifications.NotificationOptions
import org.w3c.notifications.NotificationPermission

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    if (Notification.permission != NotificationPermission.DENIED) {
        Notification.requestPermission()
    }

    ComposeViewport(document.body!!) {
        App()
    }
}