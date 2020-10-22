package com.example.simplemusic.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.simplemusic.App.Companion.appContext
import com.example.simplemusic.R
import com.example.simplemusic.activity.PlayerActivity


object NotifyHelper {
    private var notification: NotificationCompat.Builder? = null
    private val notificationChannelId by lazy { "NotifyHelper_Notification-Id" }
    private val notificationChannelName by lazy { "NotifyHelper_Notification-Name" }
    private val notificationManager by lazy { appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(notificationChannelId, notificationChannelName, NotificationManager.IMPORTANCE_MIN).apply {
                this.setShowBadge(false)
                this.enableLights(false)
                this.setBypassDnd(false)
                notificationManager.createNotificationChannel(this)
            }
        }
    }

    @JvmStatic
    fun notify(title: String, artist: String): Notification {
        val contentIntent = PendingIntent.getActivity(appContext, 0, Intent(appContext, PlayerActivity::class.java), 0)
        notification = notification ?: NotificationCompat.Builder(appContext, notificationChannelId)
        notification!!.setContentTitle(title)
                .setContentText(artist)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                .setSmallIcon(R.drawable.defult_music_img)
                .setOngoing(true)
                .setContentIntent(contentIntent)
        val build = notification!!.build()
        notificationManager.notify(999, build)
        return build
    }

    @JvmStatic
    fun cancel() {
        notificationManager.cancel(999)
    }
}