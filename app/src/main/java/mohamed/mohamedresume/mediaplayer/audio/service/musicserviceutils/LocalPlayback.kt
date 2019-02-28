package mohamed.mohamedresume.mediaplayer.audio.service.musicserviceutils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import mohamed.mohamedresume.mediaplayer.audio.service.MediaPlaybackService

class LocalPlayback(
    private val context: Context,
    private val musicQueue: MusicQueue
) : Playback {
    override fun seekTo(pos: Long) {
        exoPlayer?.seekTo(pos)
    }

    override fun getCurrentStreamPosition(): Long {
        exoPlayer?.let {
            return it.currentPosition
        }
        return 0
    }

    override fun skipToNext(step: Int) {
        musicQueue.skipQueuePosition(step)
        playCurrentQueueItem()
    }

    override fun skipToPrevious(step: Int) {
        musicQueue.skipQueuePosition(step)
        playCurrentQueueItem()
    }

    override fun playCurrentQueueItem() {
        musicQueue.currentMusic?.let {
            playQueueItem(it)
        }
    }

    override fun playQueueItem(queueItem: MediaSessionCompat.QueueItem) {

        play(MusicProvider.mediaMetadataMap[queueItem.description.mediaId]!!)
    }

    private var callback: Playback.Callback? = null
    private val mAudioNoisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

    private val mAudioNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                if (isPlaying()) {
                    pause()
                    val i = Intent(context, MediaPlaybackService::class.java)
                    i.action = MediaPlaybackService.ACTION_CMD
                    i.putExtra(MediaPlaybackService.CMD_NAME, MediaPlaybackService.CMD_PAUSE)
                    context.startService(i)
                }
            }
        }
    }


    private var exoPlayer: SimpleExoPlayer? = ExoPlayerFactory.newSimpleInstance(
        context,
        DefaultRenderersFactory(context)
        , DefaultTrackSelector(),
        DefaultLoadControl()
    ).apply {
        addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                Log.d("player", "onPlayer state changed ")
                callback!!.let {
                    when (playbackState) {
                        Player.STATE_IDLE, Player.STATE_BUFFERING, Player.STATE_READY -> {
                            it.onPlaybackStatusChanged(getState())
                        }
                        Player.STATE_ENDED -> {
                            it.onCompletion()
                        }
                    }
                }
            }
        })
    }

    override fun play(item: MediaMetadataCompat) {
        val source = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
        registerAudioNoisyReceiver()
        source?.let {
            play(extractMediaSourceFromUri(Uri.parse(it)))
        }

    }

    private fun extractMediaSourceFromUri(uri: Uri): MediaSource {
        val userAgent = Util.getUserAgent(context, "Exo")
        return ExtractorMediaSource.Factory(DefaultDataSourceFactory(context, userAgent))
            .setExtractorsFactory(DefaultExtractorsFactory()).createMediaSource(uri)
    }

    private fun play(mediaSource: MediaSource) {
        Log.d(this::class.java.simpleName, "outside exoplayer")
        exoPlayer?.apply {
            Log.d(this::class.java.simpleName, "play mediaSource")
            prepare(mediaSource)

            play()
        }
    }

    override fun play() {
        val attributes = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()
        exoPlayer?.setAudioAttributes(attributes, true)
        exoPlayer?.playWhenReady = true
    }

    private var audioNoisyReceiverRegistered = false
    private fun registerAudioNoisyReceiver() {
        if (!audioNoisyReceiverRegistered) {
            context.registerReceiver(mAudioNoisyReceiver, mAudioNoisyIntentFilter)
            audioNoisyReceiverRegistered = true
        }
    }

    private fun unregisterAudioNoisyReceiver() {
        if (audioNoisyReceiverRegistered) {
            context.unregisterReceiver(mAudioNoisyReceiver)
            audioNoisyReceiverRegistered = false
        }
    }

    override fun pause() {
        exoPlayer?.playWhenReady = false
        unregisterAudioNoisyReceiver()
        releaseResources(false)
    }

    private fun releaseResources(releasePlayer: Boolean) {
        if (releasePlayer && exoPlayer != null) {
            exoPlayer?.release()
            exoPlayer = null
            mExoPlayerNullIsStopped = true
        }
    }

    override fun isPlaying(): Boolean {
        return ((exoPlayer?.playWhenReady) ?: false)
    }

    override fun setCallback(callback: Playback.Callback) {
        this.callback = callback
    }

    private var oldUri: Uri? = null
    override fun playFromUri(uri: Uri) {
        if (oldUri != uri)
            play(extractMediaSourceFromUri(uri))
        else
            play()
        oldUri = uri
    }


    private var mExoPlayerNullIsStopped: Boolean = false

    override fun getState(): Int {
        if (exoPlayer == null) run {
            return if (mExoPlayerNullIsStopped)
                PlaybackStateCompat.STATE_STOPPED
            else
                PlaybackStateCompat.STATE_NONE
        }
        exoPlayer?.let { exoPlayer ->
            return when (exoPlayer.playbackState) {
                Player.STATE_IDLE -> PlaybackStateCompat.STATE_PAUSED
                Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
                Player.STATE_READY -> if (exoPlayer.playWhenReady)
                    PlaybackStateCompat.STATE_PLAYING
                else
                    PlaybackStateCompat.STATE_PAUSED
                Player.STATE_ENDED -> PlaybackStateCompat.STATE_PAUSED
                else -> PlaybackStateCompat.STATE_NONE
            }
        }
        return PlaybackStateCompat.STATE_NONE
    }

}