package mohamed.mohamedresume.mediaplayer.audio.service.musicserviceutils

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log

class PlaybackManager(
    private val serviceCallback: PlaybackServiceCallback,
    val resources: Resources,
    val musicQueue: MusicQueue,
    val playback: Playback,
    val context: Context
) : Playback.Callback {
    val mediaSessionCallback = MediaSessionCallback()

    init {
        playback.setCallback(this)
    }

    fun handlePlayRequest() {
        serviceCallback.onPlaybackStart()
        onPlaybackStatusChanged(PlaybackStateCompat.STATE_PLAYING)
    }

    fun handlePauseRequest() {
        if (playback.isPlaying()) {
            playback.pause()
            // ODO I think here I need to add one more call back for pause instead of stop !
            serviceCallback.onPlaybackStop()
            Log.d(this::class.java.simpleName, "state in pause request :  ${playback.getState()}")
            onPlaybackStatusChanged(PlaybackStateCompat.STATE_PAUSED)
        } else {

        }
    }

    fun updatePlaybackStatus() {
        val oldState = playback.getState()
        if (oldState != PlaybackStateCompat.STATE_PLAYING) {
            onPlaybackStatusChanged(PlaybackStateCompat.STATE_PLAYING)
        } else {
            onPlaybackStatusChanged(PlaybackStateCompat.STATE_PAUSED)
        }
    }

    fun handleStopRequest(withError: String?) {

    }

    private fun getAvailableActions(): Long {
        val actions = PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        return if (playback.isPlaying()) {
            actions or PlaybackStateCompat.ACTION_PAUSE
        } else {
            actions or PlaybackStateCompat.ACTION_PLAY
        }
    }

    override fun onCompletion() {
        musicQueue.skipQueuePosition()
        handlePlayRequest()
        playback.playCurrentQueueItem()
        Log.d("hereee", "onCompletion what happend")

    }

    override fun onPlaybackStatusChanged(state: Int) {
        val playbackState = PlaybackStateCompat.Builder().setState(state, playback.getCurrentStreamPosition(), 1f)
            .setActions(getAvailableActions())
            .build()
        serviceCallback.onPlaybackStateUpdated(playbackState)
        if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED) {
            serviceCallback.onNotificationRequired()
        }
    }

    override fun setCurrentMediaId(mediaId: String) {

    }

    inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            super.onPlay()
            handlePlayRequest()
            playback.play()
            Log.d(this::class.java.simpleName, "play man ")
        }

        override fun onStop() {
            super.onStop()
            handleStopRequest(null)
        }

        override fun onPause() {
            super.onPause()
            if (playback.isPlaying()) {
                handlePauseRequest()
                Log.d(this::class.java.simpleName, "pause man ")
            } else {
                playback.play()
                handlePlayRequest()
            }

        }

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            playback.playFromUri(uri ?: Uri.parse("assets///heart_attack.mp3"))
            handlePlayRequest()
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPlayFromMediaId(mediaId, extras)
            if (musicQueue.setQueueItemFromMediaId(mediaId ?: ""))
                playback.playQueueItem(musicQueue.currentMusic!!)
            else
                playback.play()

            handlePlayRequest()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            playback.skipToNext()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            playback.skipToPrevious()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            playback.seekTo(pos)
        }
    }

    interface PlaybackServiceCallback {
        fun onPlaybackStart()

        fun onNotificationRequired()

        fun onPlaybackStop()

        fun onPlaybackStateUpdated(newState: PlaybackStateCompat)
    }
}
