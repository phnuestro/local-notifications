package com.paolo.plugins.localnotifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nativephp.mobile.bridge.BridgeFunction
import com.nativephp.mobile.bridge.BridgeResponse
import com.nativephp.mobile.bridge.NativeContext

object LocalNotificationFunctions {

    private const val EXTRA_ID = "com.paolo.plugins.localnotifications.EXTRA_ID"
    private const val EXTRA_TITLE = "com.paolo.plugins.localnotifications.EXTRA_TITLE"
    private const val EXTRA_BODY = "com.paolo.plugins.localnotifications.EXTRA_BODY"
    private const val EXTRA_DATA = "com.paolo.plugins.localnotifications.EXTRA_DATA"
    private const val EXTRA_CHANNEL_ID = "com.paolo.plugins.localnotifications.EXTRA_CHANNEL_ID"
    private const val EXTRA_CHANNEL_NAME = "com.paolo.plugins.localnotifications.EXTRA_CHANNEL_NAME"
    private const val EXTRA_SOUND = "com.paolo.plugins.localnotifications.EXTRA_SOUND"
    private const val EXTRA_BADGE = "com.paolo.plugins.localnotifications.EXTRA_BADGE"

    class RequestPermission : BridgeFunction {
        override fun execute(parameters: Map<String, Any>): Map<String, Any> {
            val context = NativeContext.get()

            // Android < 13 does not require a runtime permission for notifications.
            val granted = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                true
            } else {
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            }

            return BridgeResponse.success(mapOf("granted" to granted))
        }
    }

    class Schedule : BridgeFunction {
        override fun execute(parameters: Map<String, Any>): Map<String, Any> {
            val context = NativeContext.get()
            val id = parameters["id"] as? String
                ?: return BridgeResponse.error("Missing required 'id' parameter")
            val title = parameters["title"] as? String ?: ""
            val body = parameters["body"] as? String ?: ""
            val delay = (parameters["delay"] as? Number)?.toLong()
            val at = (parameters["at"] as? Number)?.toLong()
            val channelId = parameters["channelId"] as? String ?: "default"
            val channelName = parameters["channelName"] as? String ?: "General"
            val soundEnabled = parameters["sound"] != false
            val badge = (parameters["badge"] as? Number)?.toInt()
            val data = (parameters["data"] as? Map<*, *>)
                ?.entries
                ?.joinToString("&") { "${it.key}=${it.value}" } ?: ""

            ensureChannel(context, channelId, channelName)

            val triggerAtMillis = when {
                at != null -> at * 1000L
                delay != null -> System.currentTimeMillis() + (delay * 1000L)
                else -> System.currentTimeMillis() // fire immediately
            }

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra(EXTRA_ID, id)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_BODY, body)
                putExtra(EXTRA_DATA, data)
                putExtra(EXTRA_CHANNEL_ID, channelId)
                putExtra(EXTRA_CHANNEL_NAME, channelName)
                putExtra(EXTRA_SOUND, soundEnabled)
                badge?.let { putExtra(EXTRA_BADGE, it) }
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (delay == null && at == null) {
                // No timing supplied — show it right away instead of scheduling an alarm.
                context.sendBroadcast(intent)
            } else {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            }

            return BridgeResponse.success(mapOf("id" to id, "scheduledFor" to triggerAtMillis))
        }

        private fun ensureChannel(context: Context, channelId: String, channelName: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (manager.getNotificationChannel(channelId) == null) {
                    manager.createNotificationChannel(
                        NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
                    )
                }
            }
        }
    }

    class Cancel : BridgeFunction {
        override fun execute(parameters: Map<String, Any>): Map<String, Any> {
            val context = NativeContext.get()
            val id = parameters["id"] as? String
                ?: return BridgeResponse.error("Missing required 'id' parameter")

            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)

            NotificationManagerCompat.from(context).cancel(id.hashCode())

            return BridgeResponse.success(mapOf("cancelled" to true))
        }
    }

    class CancelAll : BridgeFunction {
        override fun execute(parameters: Map<String, Any>): Map<String, Any> {
            val context = NativeContext.get()
            NotificationManagerCompat.from(context).cancelAll()

            return BridgeResponse.success(mapOf("cancelled" to true))
        }
    }
}
