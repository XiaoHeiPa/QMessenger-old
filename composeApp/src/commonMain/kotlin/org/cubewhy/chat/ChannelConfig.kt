package org.cubewhy.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController

@Composable
fun ChannelConfig(nav: NavController, id: Long) {
    LaunchedEffect(id) {
        val channel = QMessenger.channelConf(id)
    }
}