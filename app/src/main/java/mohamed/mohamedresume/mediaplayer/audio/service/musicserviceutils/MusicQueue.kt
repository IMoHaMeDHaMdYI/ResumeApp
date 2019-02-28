package mohamed.mohamedresume.mediaplayer.audio.service.musicserviceutils

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat

class MusicQueue(private val metadataUpdateListener: MetadataUpdateListener) {
    var currentMusic: MediaSessionCompat.QueueItem? = null
    var currentMetaData: MediaMetadataCompat? = null
    var itemId = 0L
    var mCurrentQueue: ArrayList<MediaSessionCompat.QueueItem> = ArrayList()
    fun setQueueItem(item: MediaSessionCompat.QueueItem) {
        currentMusic = item
        updateMetadata(MusicProvider.mediaMetadataMap[item.description.mediaId]!!)
    }

    fun updateMetadata(metaData: MediaMetadataCompat) {
        metadataUpdateListener.onMetadataChanged(metaData)
    }

    private fun convertToQueue(
        track: MediaDescriptionCompat
    ): MediaSessionCompat.QueueItem {
        return MediaSessionCompat.QueueItem(
            track,
            ++itemId
        )
    }

    fun getQueue(mediaId: String): ArrayList<MediaSessionCompat.QueueItem> {
        val queue = ArrayList<MediaSessionCompat.QueueItem>()
        MusicProvider.treeMap[mediaId]?.children?.forEach {
            queue.add(convertToQueue(it.data.description))
        }
        return queue
    }

    fun setQueueItemFromMetadata(metaData: MediaMetadataCompat) {
        val item = convertToQueue(metaData.description)
        currentMetaData = metaData
        setQueueItem(item)
    }

    fun setQueueItemFromMediaId(mediaId: String): Boolean {
        if (mediaId != currentMusic?.description?.mediaId) {
            setQueueItemFromMetadata(MusicProvider.mediaMetadataMap[mediaId]!!)
            return true
        }
        return false
    }

    fun setQueue(parentId: String) {
        mCurrentQueue = getQueue(parentId)
        metadataUpdateListener.onQueueUpdated("title", mCurrentQueue!!)
    }

    fun skipQueuePosition(amount: Int = 1) {
        currentMusic?.apply {
            val currentIndex = queueId.toInt()
            setQueueItem(mCurrentQueue[(currentIndex + amount) % mCurrentQueue.size])
        }
    }

    interface MetadataUpdateListener {
        fun onMetadataChanged(metadata: MediaMetadataCompat)
        fun onMetadataRetrieveError()
        fun onCurrentQueueIndexUpdated(queueIndex: Int)
        fun onQueueUpdated(title: String, newQueue: List<MediaSessionCompat.QueueItem>)
    }
}