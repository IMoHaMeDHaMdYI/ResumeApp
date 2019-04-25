package mohamed.mohamedresume.mediaplayer.audio.service.musicserviceutils

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat

interface Playback {
    fun play(item: MediaMetadataCompat)
    fun play()
    fun pause()
    fun isPlaying(): Boolean
    fun playFromUri(uri: Uri)
    fun playQueueItem(queueItem: MediaSessionCompat.QueueItem)
    fun getState(): Int
    fun playCurrentQueueItem()
    fun skipToNext(step: Int = 1)
    fun skipToPrevious(step: Int = -1)
    fun getCurrentStreamPosition(): Long
    fun seekTo(pos : Long)

    interface Callback {
        fun onCompletion()
        fun onPlaybackStatusChanged(state: Int)
        fun setCurrentMediaId(mediaId: String)
    }

    fun setCallback(callback: Callback)
}