package org.cubewhy.chat

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource
import qmessenger.composeapp.generated.resources.Res
import qmessenger.composeapp.generated.resources.logo

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        icon = painterResource(Res.drawable.logo),
        title = "QMessenger",
    ) {
        App()
    }
}
