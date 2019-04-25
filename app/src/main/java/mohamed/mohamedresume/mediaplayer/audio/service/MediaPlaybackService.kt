package mohamed.mohamedresume.mediaplayer.audio.service

import android.content.Intent
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.SimpleExoPlayer
import mohamed.mohamedresume.mediaplayer.audio.service.musicserviceutils.LocalPlayback
import mohamed.mohamedresume.mediaplayer.audio.service.musicserviceutils.MusicProvider
import mohamed.mohamedresume.mediaplayer.audio.service.musicserviceutils.MusicQueue
import mohamed.mohamedresume.mediaplayer.audio.service.musicserviceutils.PlaybackManager


private const val MY_MEDIA_ROOT_ID = "root"

class MediaPlaybackService : MediaBrowserServiceCompat(), PlaybackManager.PlaybackServiceCallback {
    override fun onPlaybackStart() {
        mMediaSession?.isActive = true

        // This line helped with saving the state of played or paused as without it if I exit the activity and come back
        // and try to pause the song it starts from the beginning instead of pausing
        startService(Intent(applicationContext, MediaPlaybackService::class.java))
    }

    override fun onNotificationRequired() {
        mMusicNotification.startNotification()
    }

    override fun onPlaybackStop() {
        mMediaSession?.isActive = false
        stopForeground(false)
    }

    override fun onPlaybackStateUpdated(newState: PlaybackStateCompat) {
        Log.d(TAG, "state is ${newState.state}")
        mMediaSession?.setPlaybackState(newState)
    }


    private var mMediaSession: MediaSessionCompat? = null
    private lateinit var mStateBuilder: PlaybackStateCompat.Builder
    private val TAG = this::class.java.simpleName
    private var exoplayer: SimpleExoPlayer? = null
    private lateinit var playbackManager: PlaybackManager
    private lateinit var mMusicNotification: MusicNotification

    companion object {
        val ACTION_CMD = "action_command"
        val CMD_NAME = "command_name"
        val CMD_PAUSE = "command_pause"
    }

    lateinit var musicQueue: MusicQueue
    override fun onCreate() {
        super.onCreate()

        Log.d(this::class.java.simpleName, "Created")
        musicQueue = MusicQueue(object : MusicQueue.MetadataUpdateListener {
            override fun onMetadataChanged(metadata: MediaMetadataCompat) {
                mMediaSession?.setMetadata(metadata)
            }

            override fun onMetadataRetrieveError() {

            }

            override fun onCurrentQueueIndexUpdated(queueIndex: Int) {
            }

            override fun onQueueUpdated(title: String, newQueue: List<MediaSessionCompat.QueueItem>) {
                mMediaSession?.setQueue(newQueue)
                mMediaSession?.setQueueTitle(title)
            }

        })

        val playback = LocalPlayback(this, musicQueue)
        playbackManager = PlaybackManager(this, resources, musicQueue, playback, this)

        mMediaSession = MediaSessionCompat(baseContext, TAG).apply {
            // Enable callbacks from MediaButtons and TransportControls
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            // Set initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            mStateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)
            setPlaybackState(mStateBuilder.build())

            // MySessionCallback() has methods that handle callbacks from a media controller
            setCallback(playbackManager.mediaSessionCallback)

            // Set the session's token so that client activities can communicate with it
            setSessionToken(sessionToken)
            isActive = true
        }
        try {
            mMusicNotification = MusicNotification(this)
        } catch (e: RemoteException) {

        }

    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        val mediaItems = getChildren(parentId)
        musicQueue.setQueue(parentId)
        result.sendResult(mediaItems)


    }

    private fun getChildren(mediaId: String): ArrayList<MediaBrowserCompat.MediaItem> {
        val l = ArrayList<MediaBrowserCompat.MediaItem>()
        // MediaNode#children is not nullable WTF !!
        MusicProvider.treeMap[mediaId]?.children?.forEach {
            l.add(it.data)
        }
        return l
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {

        return MediaBrowserServiceCompat.BrowserRoot(MY_MEDIA_ROOT_ID, null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val action = it.action
            val cmd = intent.getStringExtra(CMD_NAME)
            if (ACTION_CMD == action)
                if (cmd == CMD_PAUSE) {
                    playbackManager.handlePauseRequest()
                }
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "destroyed")
        mMediaSession?.release()
        mMusicNotification.stopNotification()
        playbackManager.handleStopRequest(null)
    }
}