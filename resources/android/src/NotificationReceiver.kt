package com.phnuestro.plugins.localnotifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nativephp.mobile.bridge.NativeEventDispatcher

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("com.phnuestro.plugins.localnotifications.EXTRA_ID") ?: return
        val title = intent.getStringExtra("com.phnuestro.plugins.localnotifications.EXTRA_TITLE") ?: ""
        val body = intent.getStringExtra("com.phnuestro.plugins.localnotifications.EXTRA_BODY") ?: ""
        val channelId = intent.getStringExtra("com.phnuestro.plugins.localnotifications.EXTRA_CHANNEL_ID") ?: "default"

        val tapIntent = Intent(context, NotificationTapReceiver::class.java).apply {
            putExtra("id", id)
        }
        val tapPendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(context.applicationInfo.icon)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(id.hashCode(), notification)
    }
}

/**
 * Fires the NotificationTapped PHP event when the user taps a delivered notification.
 */
class NotificationTapReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("id") ?: return

        NativeEventDispatcher.dispatch(
            "Phnuestro\\LocalNotifications\\Events\\NotificationTapped",
            mapOf("id" to id, "data" to emptyMap<String, Any>())
        )
    }
}
