package mohamed.mohamedresume.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.loader.content.Loader
import mohamed.mohamedresume.R

class AlarmReceiver : BroadcastReceiver() {
    private val CHANNEL_ID = "alarm channel"
    private val NOTIFICATION_CHANNEL = "alarm notification"
    private lateinit var mNotificationManager: NotificationManager
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("TAAAAAAG", "TAAAAAAAAAAAAg")
        context?.let {
            val sound = Uri.parse("android.resource://" + it.packageName + R.raw.heart_attack)
            mNotificationManager = it.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                createNotificationChannel(sound)
            val notificationBuilder = NotificationCompat.Builder(it, CHANNEL_ID)
            val notification = notificationBuilder.setSmallIcon(android.R.drawable.ic_media_play)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS or NotificationCompat.DEFAULT_VIBRATE)
                .setContentText("ALARM")
                .setContentTitle("ALARM")
                .setSound(sound)
                .setVibrate(arrayOf(3000L, 4000L).toLongArray())
                .build()
            mNotificationManager.notify(0, notification)
            Log.d("TAAAAAAG", "TAAAAAAAAAAAAg")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(sound: Uri) {
        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                NOTIFICATION_CHANNEL,
                NotificationManager.IMPORTANCE_HIGH
            )
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            notificationChannel.setSound(
                sound,
                audioAttributes
            )

            notificationChannel.description = "Alarm Notification"

            mNotificationManager.createNotificationChannel(notificationChannel)
        }
    }
}