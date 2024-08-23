package org.cubewhy.chat

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import org.jetbrains.compose.resources.getString


actual fun pushNotification(title: String, body: String) {
    val activity = helper.activity!!
    val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val notificationBuilder =
        NotificationCompat.Builder(activity, activity.getString(R.string.push_channel_buildin))
            .setContentTitle(title)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)

    notificationManager.notify(666, notificationBuilder.build())
}