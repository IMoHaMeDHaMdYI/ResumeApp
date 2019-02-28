package mohamed.mohamedresume.mediaplayer.audio.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.RemoteException
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import mohamed.mohamedresume.R
import mohamed.mohamedresume.mediaplayer.audio.ui.TestActivity


class MusicNotification(private val mService: MediaPlaybackService) : BroadcastReceiver() {
    private var mSessionToken: MediaSessionCompat.Token? = null
    private var mController: MediaControllerCompat? = null
    private var mTransportControls: MediaControllerCompat.TransportControls? = null
    private var mPlaybackState: PlaybackStateCompat? = null
    private var mMetadata: MediaMetadataCompat? = null
    private val mNotificationManager: NotificationManager =
        mService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val NOTIFICATION_ID = 444
    private val CHANNEL_ID = "com.example.android.uamp.MUSIC_CHANNEL_ID"
    private var mStarted = false
    private val mCb = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            mPlaybackState = state
            state?.let {
                if (it.state == PlaybackStateCompat.STATE_STOPPED ||
                    it.state == PlaybackStateCompat.STATE_NONE
                )
                    stopNotification()
                else {
                    val notification = createNotification()
                    notification?.apply { mNotificationManager.notify(NOTIFICATION_ID, this) }
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mMetadata = metadata
            val notification = createNotification()
            notification?.apply { mNotificationManager.notify(NOTIFICATION_ID, notification) }
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            try {
                updateSessionToken()
            } catch (e: RemoteException) {

            }
        }
    }

    private val mPauseIntent: PendingIntent
    private val mPlayIntent: PendingIntent
    private val mNextIntent: PendingIntent
    private val mPreviousIntent: PendingIntent
    private val mStopIntent: PendingIntent


    private val ACTION_PAUSE = "pause"
    private val ACTION_PLAY = "play"
    private val ACTION_PREV = "prev"
    private val ACTION_NEXT = "next"
    private val ACTION_STOP = "stop"

    init {
        updateSessionToken()
        val pkg = mService.packageName
        mPauseIntent = PendingIntent.getBroadcast(
            mService, 0, Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT
        )
        mPlayIntent = PendingIntent.getBroadcast(
            mService, 0, Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT
        )
        mPreviousIntent = PendingIntent.getBroadcast(
            mService, 0, Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT
        )
        mNextIntent = PendingIntent.getBroadcast(
            mService, 0, Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT
        )
        mStopIntent = PendingIntent.getBroadcast(
            mService, 0, Intent(ACTION_STOP).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT
        )

        mNotificationManager.cancelAll()
    }

    fun startNotification() {
        if (!mStarted) {
            mMetadata = mController?.metadata
            mPlaybackState = mController?.playbackState

            val notification = createNotification()
            notification?.let {
                mController?.registerCallback(mCb)
                val filter = IntentFilter()
                filter.apply {
                    addAction(ACTION_NEXT)
                    addAction(ACTION_PREV)
                    addAction(ACTION_PAUSE)
                    addAction(ACTION_PLAY)
                    addAction(ACTION_STOP)
                    mService.registerReceiver(this@MusicNotification, this)
                    mService.startForeground(NOTIFICATION_ID, it)
                    mStarted = true
                }
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        mTransportControls?.let {
            when (action) {
                ACTION_PAUSE -> it.pause()
                ACTION_PLAY -> it.play()
                ACTION_STOP -> it.stop()
                ACTION_NEXT -> it.skipToNext()
                ACTION_PREV -> it.skipToPrevious()
                else -> Log.d(this::class.java.simpleName, "We have a problem in the action")
            }
        }

    }

    @Throws(RemoteException::class)
    fun updateSessionToken() {
        val token = mService.sessionToken
        if (mSessionToken == null && token != null || mSessionToken != null && mSessionToken!! != token) {
            mController?.let {
                it.unregisterCallback(mCb)
            }
            mSessionToken = token
            mSessionToken?.let {
                mController = MediaControllerCompat(mService, it)
                mTransportControls = mController?.transportControls
                if (mStarted) {
                    mController?.registerCallback(mCb)
                }
            }
        }
    }


    fun createNotification(): Notification? {
        if (mMetadata == null || mPlaybackState == null) {
            return null
        }
        mMetadata?.let { metadata ->
            mPlaybackState?.let { state ->
                val description = metadata.description
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    createNotificationChannel()
                val notificationBuilder = NotificationCompat.Builder(mService, CHANNEL_ID)
                val playPauseBtnPos = addActions(notificationBuilder)
                notificationBuilder.setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(playPauseBtnPos)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(mStopIntent)
                        .setMediaSession(mSessionToken)
                )
                    .setDeleteIntent(mStopIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(android.R.drawable.stat_notify_chat)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(createContentIntent(description))
                    .setContentTitle(description.title)
                    .setContentText(description.subtitle)
                setNotificationPlaybackState(notificationBuilder)
                return notificationBuilder.build()
            }
        }
        return null
    }

    fun stopNotification() {
        if (mStarted) {
            mStarted = false
            mController?.unregisterCallback(mCb)
            try {
                mNotificationManager.cancel(NOTIFICATION_ID)
                mService.unregisterReceiver(this)
            } catch (e: IllegalArgumentException) {

            }
            mService.stopForeground(true)
        }
    }

    private fun addActions(notificationBuilder: NotificationCompat.Builder): Int {
        var playPauseBtnPos = 0
        mPlaybackState?.let {
            if ((it.actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0L) {
                notificationBuilder.addAction(android.R.drawable.ic_media_previous, "Previous", mPreviousIntent)
                playPauseBtnPos = 1
            }
            val label: String
            val icon: Int
            val intent: PendingIntent
            if (it.state == PlaybackStateCompat.STATE_PLAYING) {
                label = "Pause"
                icon = android.R.drawable.ic_media_pause
                intent = mPauseIntent
            } else {
                label = "Play"
                icon = android.R.drawable.ic_media_play
                intent = mPlayIntent
            }
            notificationBuilder.addAction(NotificationCompat.Action(icon, label, intent))
            if ((it.actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0L) {
                notificationBuilder.addAction(android.R.drawable.ic_media_next, "Next", mNextIntent)
            }
        }
        return playPauseBtnPos
    }

    private fun setNotificationPlaybackState(builder: NotificationCompat.Builder) {
        mPlaybackState?.let {
            if (!mStarted) {
                mService.stopForeground(true)
                return
            }
            builder.setOngoing(it.state == PlaybackStateCompat.STATE_PLAYING)
            return
        }
        mService.stopForeground(true)
    }

    private fun createContentIntent(description: MediaDescriptionCompat): PendingIntent {
        val openUI = Intent(mService, TestActivity::class.java)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        return PendingIntent.getActivity(mService, 0, openUI, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                mService.getString(R.string.notification_channel),
                NotificationManager.IMPORTANCE_LOW
            )

            notificationChannel.description = "Music Notification"

            mNotificationManager.createNotificationChannel(notificationChannel)
        }
    }
}
