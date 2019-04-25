package mohamed.mohamedresume.mediaplayer.audio.service.musicserviceutils

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat

// Music will be the same, they are pre-downloaded into the assets folder
object MusicProvider {
    val uriList = arrayListOf("asset:///heart_attack.mp3", "asset:///heart_attack.mp3")

    var i = 0
    val mediaMetadataList = uriList.map {
        MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, it)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "Sourat Al-Fatehah")
                .putString(MediaMetadataCompat.METADATA_KEY_AUTHOR, "God")
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, "Quran")
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "s${++i}")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 44000L)
                .build()
    }
    val mediaMetadataMap = HashMap<String, MediaMetadataCompat>().apply {
        mediaMetadataList.forEach {
            this[it.description.mediaId!!] = it
        }
    }
    val mediaItemList = mediaMetadataList.map {
        MediaBrowserCompat.MediaItem(it.description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }
    val mediaGenreDescription = arrayListOf(
            MediaDescriptionCompat.Builder().setMediaId("Quran")
                    .setTitle("Quran")
                    .build()
    )
    val genreMediaItemList = mediaGenreDescription.map {
        MediaBrowserCompat.MediaItem(it, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
    }
    var x = 0
    val treeMap =
            HashMap<String, MediaNode<MediaBrowserCompat.MediaItem>>().apply {
                this["root"] = MediaNode(
                        MediaBrowserCompat.MediaItem(
                                MediaDescriptionCompat.Builder().setMediaId("root").setTitle("root").build(),
                                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                        )
                )
                genreMediaItemList.forEach {
                    this[it.mediaId ?: "${++x}"] = MediaNode(it, this["root"])
                    mediaItemList.forEach { mediaItem ->
                        this[mediaItem.mediaId!!] = MediaNode(mediaItem, this[it.mediaId ?: "$x"])
                        this[it.mediaId ?: "$x"]!!.children.add(this[mediaItem.mediaId!!]!!)
                    }
                    this["root"]!!.children.add(this[it.mediaId ?: "$x"]!!)
                }
            }
}
