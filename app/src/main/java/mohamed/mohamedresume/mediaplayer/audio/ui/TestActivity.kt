package mohamed.mohamedresume.mediaplayer.audio.ui

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import mohamed.mohamedresume.R
import mohamed.mohamedresume.mediaplayer.audio.service.MediaPlaybackService
import mohamed.mohamedresume.mediaplayer.audio.service.musicserviceutils.MusicProvider

class TestActivity : AppCompatActivity(), BrowserFragment.IBrowserFragmentCallback {
    override fun onMediaItemClicked(mediaId: String) {
        navigateToBrowserWithId(mediaId)
    }

    private lateinit var mediaBrowserCompat: MediaBrowserCompat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        initializeMediaBrowser()
    }

    private fun initializeMediaBrowser() {
        val componentName = ComponentName(baseContext, MediaPlaybackService::class.java)
        mediaBrowserCompat = MediaBrowserCompat(
                this, componentName,
                object : MediaBrowserCompat.ConnectionCallback() {
                    override fun onConnected() {
                        super.onConnected()
                        mediaBrowserCompat.sessionToken.also { token ->
                            val mediaController = MediaControllerCompat(this@TestActivity, token)
                            MediaControllerCompat.setMediaController(this@TestActivity, mediaController)
                        }
                        navigateToBrowserWithId("root")
                        showController()
                    }

                    override fun onConnectionFailed() {
                        super.onConnectionFailed()
                        Log.d("onConnectionFailed", "Connection Failed")

                    }

                }, null
        )


    }

    override fun onStop() {
        super.onStop()
        val controllerCompat = MediaControllerCompat.getMediaController(this)
        controllerCompat?.unregisterCallback(controllerCallback)
        mediaBrowserCompat.disconnect()
    }

    override fun onStart() {
        super.onStart()
        if (!mediaBrowserCompat.isConnected) {
            mediaBrowserCompat.connect()

        }
    }

    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            Log.d(this::class.java.simpleName, "state changed ${state?.state}")
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            Log.d(
                    this::class.java.simpleName, "metadata changed : ${metadata?.description?.title
                    ?: "No title"}"
            )
        }
    }

    fun navigateToBrowserWithId(mediaId: String) {
        val node = MusicProvider.treeMap[mediaId]!!
        val browserFragment =
                BrowserFragment.createFragmentWithRoot(
                        node,
                        mediaBrowserCompat,
                        this@TestActivity
                )
        val transaction = supportFragmentManager.beginTransaction()
                .replace(R.id.viewBrowser, browserFragment, "BrowserFragment")
        if (node.data.isBrowsable) {
//            transaction.addToBackStack(null)
        }
        transaction.commit()
    }

    fun showController() {
        supportFragmentManager.beginTransaction().replace(
                R.id.viewController, ControllerFragment.createFragment(mediaBrowserCompat),
                "Controller"
        ).commit()
    }

    override fun onBackPressed() {
//        if (supportFragmentManager.backStackEntryCount > 0) {
//            supportFragmentManager.popBackStack()
//        } else
        super.onBackPressed()
    }
}
