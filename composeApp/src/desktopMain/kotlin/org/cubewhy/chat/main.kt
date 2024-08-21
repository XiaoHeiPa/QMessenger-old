package org.cubewhy.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberNotification
import androidx.compose.ui.window.rememberTrayState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import qmessenger.composeapp.generated.resources.Res
import qmessenger.composeapp.generated.resources.app_name
import qmessenger.composeapp.generated.resources.logo

fun main() = application {
    val trayState = rememberTrayState()
    var isOpen by remember { mutableStateOf(true) }

    if (isOpen) {

        Tray(
            state = trayState,
            icon = TrayIcon,
            tooltip = stringResource(Res.string.app_name)
        )

        Window(
            onCloseRequest = ::exitApplication,
            icon = painterResource(Res.drawable.logo),
            title = "QMessenger",
        ) {
            App()
        }
    }
}

object TrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color(0xFFFFA500))
    }
}
