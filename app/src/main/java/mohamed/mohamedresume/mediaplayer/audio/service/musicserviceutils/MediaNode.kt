package mohamed.mohamedresume.mediaplayer.audio.service.musicserviceutils

class MediaNode<T>(
    var data: T,
    var parent: MediaNode<T>? = null,
    val children: ArrayList<MediaNode<T>> = ArrayList()
) {
    override fun toString(): String {
        return "data = $data , parent = ${parent?.data} , children ${children.map { it.data }} "

    }
}

