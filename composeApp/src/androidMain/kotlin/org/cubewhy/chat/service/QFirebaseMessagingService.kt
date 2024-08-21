package org.cubewhy.chat.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.coroutines.runBlocking
import org.cubewhy.chat.QMessenger

class QFirebaseMessagingService: FirebaseMessagingService() {
    companion object {
        private const val TAG = "FCM Service"
    }

    override fun onNewToken(token: String) {
        runBlocking {
            Log.v(TAG, "Update FCM token ($token)")
            QMessenger.fcm(token)
        }
    }
}