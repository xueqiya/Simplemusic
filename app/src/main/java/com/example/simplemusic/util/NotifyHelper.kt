package com.example.simplemusic.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.simplemusic.App.Companion.appContext
import com.example.simplemusic.R

object NotifyHelper {
    private var notification: NotificationCompat.Builder? = null
    private val notificationChannelId by lazy { "NotifyHelper_Notification-Id" }
    private val notificationChannelName by lazy { "NotifyHelper_Notification-Name" }
    private val notificationManager by lazy { appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                    notificationChannelId, notificationChannelName,
                    NotificationManager.IMPORTANCE_LOW
            ).apply {
                this.setShowBadge(true)
                this.enableLights(true)
                this.lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
                this.setBypassDnd(true)
                notificationManager.createNotificationChannel(this)
            }
        }
    }

    @JvmStatic
    fun notify(title: String, artist: String): Notification {
        notification = notification
                ?: NotificationCompat.Builder(appContext, notificationChannelId)
                        .setContentTitle(title)
                        .setContentText(artist)
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setSmallIcon(R.drawable.defult_music_img)
                        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                        .setOngoing(true)
                        .setAutoCancel(true)
        val build = notification!!.build()
        notificationManager.notify(999, build)
        return build
    }

    @JvmStatic
    fun cancel() {
        notificationManager.cancel(999)
    }
}