package org.cubewhy.chat.util

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import org.cubewhy.chat.R

fun String.requestPermission(activity: Activity) {
    if (Build.VERSION.SDK_INT >= 33) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                this
            ) == PackageManager.PERMISSION_DENIED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    this
                )
            ) {
                ActivityCompat.requestPermissions(activity, arrayOf(this), 100)
            }
        }
    }
}

fun Context.launchSettings() {
    try {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, this.packageName)
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, this.applicationInfo.uid)
        }
        intent.putExtra("app_package", this.packageName)
        intent.putExtra("app_uid", this.applicationInfo.uid)
        this.startActivity(intent)
    } catch (e: Exception) {
        val intent = Intent()
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", this.packageName, null)
        intent.setData(uri)
        this.startActivity(intent)
    }
}

fun Activity.createNotificationChannel(channelName: String, description: String? = null, importance: Int = NotificationManager.IMPORTANCE_DEFAULT) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Create the NotificationChannel.
        val mChannel = NotificationChannel(getString(R.string.push_channel), channelName, importance)
        mChannel.description = description
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }
}