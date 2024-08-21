package org.cubewhy.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController

@Composable
fun ChatScreen(nav: NavController) {
    val channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
}