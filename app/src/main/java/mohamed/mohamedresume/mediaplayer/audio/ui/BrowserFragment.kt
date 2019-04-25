package mohamed.mohamedresume.mediaplayer.audio.ui


import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_browser.*
import mohamed.mohamedresume.R
import mohamed.mohamedresume.mediaplayer.audio.service.musicserviceutils.MediaNode
import mohamed.mohamedresume.mediaplayer.audio.service.musicserviceutils.MusicProvider

private lateinit var mMediaNode: MediaNode<MediaBrowserCompat.MediaItem>
private lateinit var mMediaBrowser: MediaBrowserCompat
private lateinit var mCallback: BrowserFragment.IBrowserFragmentCallback

class BrowserFragment : Fragment() {
    private lateinit var mediaAdapter: MediaAdapter
    private val TAG = this::class.java.simpleName
    private val mSubscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
            super.onChildrenLoaded(parentId, children)
            Log.d(TAG, "onChildrenLoaded : $parentId //\\ ${children.size}")
            // only take the playable children
            mediaAdapter.clear()
            children.filter {
                Log.d(TAG, "playable ? : ${it.isPlayable} + ${it.mediaId}")
                it.isPlayable && MusicProvider.treeMap[it.mediaId] != null
            }
                .forEach {
                    mediaAdapter.add(MusicProvider.treeMap[it.mediaId ?: ""]!!)
                    Log.d(TAG, "media id : ${it.mediaId}")
                }
            // Only take the browsable children.
            children.filter {
                it.isBrowsable && MusicProvider.treeMap[it.mediaId] != null
            }.forEach {
                mediaAdapter.add(MusicProvider.treeMap[it.mediaId ?: ""]!!)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_browser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaAdapter = MediaAdapter(view.context, ArrayList()) {
            mCallback.onMediaItemClicked(it)
            if (MusicProvider.treeMap[it]!!.data.isBrowsable)
                mMediaBrowser.subscribe(it, mSubscriptionCallback)
            else if (MusicProvider.treeMap[it]!!.data.isPlayable)
                activity?.apply {
                    MediaControllerCompat.getMediaController(this).transportControls.playFromMediaId(it, null)
                }
        }
        rvMedia?.apply {
            adapter = mediaAdapter
            layoutManager = LinearLayoutManager(view.context)
        }

    }

    override fun onStart() {
        super.onStart()
        Log.d("start", "OnStart")
        if (mMediaBrowser.isConnected) {
            mMediaBrowser.subscribe(
                mMediaNode.data.mediaId
                    ?: "", mSubscriptionCallback
            )
        }
    }

    override fun onStop() {
        super.onStop()
        mMediaBrowser.unsubscribe(mMediaNode.data.mediaId ?: "")
    }

    companion object {
        private val instance: BrowserFragment by lazy { BrowserFragment() }
        fun createFragmentWithRoot(
            mediaNode: MediaNode<MediaBrowserCompat.MediaItem>
            , mediaBrowser: MediaBrowserCompat
            , callback: IBrowserFragmentCallback
        ): BrowserFragment {
            Log.d(
                "BrowserFragment", "node : $mediaNode , browser ${mediaBrowser.root}"
            )
            mMediaNode = mediaNode
            mMediaBrowser = mediaBrowser
            mCallback = callback
            return instance
        }
    }

    interface IBrowserFragmentCallback {
        fun onMediaItemClicked(mediaId: String)
    }
}
