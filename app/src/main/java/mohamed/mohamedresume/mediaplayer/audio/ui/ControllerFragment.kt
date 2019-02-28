package mohamed.mohamedresume.mediaplayer.audio.ui


import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_controller.*
import mohamed.mohamedresume.R
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

private lateinit var mMediaBrowser: MediaBrowserCompat

class ControllerFragment : Fragment() {
    private val PROGRESS_UPDATE_INTERNAL: Long = 1000
    private val PROGRESS_UPDATE_INITIAL_INTERVAL: Long = 100


    private lateinit var mController: MediaControllerCompat
    private var mLastPlaybackState: PlaybackStateCompat? = null

    private val mHandler = Handler()
    private val mUpdateProgressTask = Runnable { updateProgress() }
    private lateinit var mExecutorService: ScheduledExecutorService

    private var mScheduleFuture: ScheduledFuture<*>? = null


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_controller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mExecutorService = Executors.newSingleThreadScheduledExecutor()
        btnPlay.setOnClickListener {
            val state = mController.playbackState.state
            if (state == PlaybackStateCompat.STATE_PAUSED ||
                    state == PlaybackStateCompat.STATE_STOPPED ||
                    state == PlaybackStateCompat.STATE_NONE
            ) {
                mController.transportControls.play()
            } else if (state == PlaybackStateCompat.STATE_PLAYING ||
                    state == PlaybackStateCompat.STATE_BUFFERING ||
                    state == PlaybackStateCompat.STATE_CONNECTING
            ) {
                mController.transportControls.pause()
            }
        }
        btnNext.setOnClickListener {
            mController.transportControls.skipToNext()
        }
        btnPrevious.setOnClickListener {
            mController.transportControls.skipToPrevious()
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                stopSeekBarUpdate()
            }


            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mController.transportControls.seekTo(seekBar!!.progress.toLong())
                scheduleSeekbarUpdate()
            }

        })
        controllerCallback.onPlaybackStateChanged(mController.playbackState)
        controllerCallback.onMetadataChanged(mController.metadata)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (mMediaBrowser.isConnected) {
            mController = MediaControllerCompat.getMediaController(activity!!)
            mController.registerCallback(controllerCallback)
        }

    }

    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            state?.let {
                mLastPlaybackState = it
                Log.d(this::class.java.simpleName, "last play back pos = ${it.position}")
                if (it.state != PlaybackStateCompat.STATE_PLAYING) {
                    setPlayState(true)
                    stopSeekBarUpdate()
                } else {
                    setPlayState(false)
                    scheduleSeekbarUpdate()
                }
            }
            Log.d(this::class.java.simpleName, "state changed ${state?.state}")
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            Log.d(
                    this::class.java.simpleName, "metadata changed : ${metadata?.description?.title
                    ?: "No title"} then for max : ${
            (metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)?.toInt()) ?: 2
            }"
            )
            seekBar.max = (metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)?.toInt()) ?: 2
        }
    }

    override fun onDetach() {
        super.onDetach()
        mController.unregisterCallback(controllerCallback)
    }

    companion object {
        val instance: ControllerFragment by lazy {
            ControllerFragment()
        }

        fun createFragment(mediaBrowser: MediaBrowserCompat): ControllerFragment {
            mMediaBrowser = mediaBrowser
            return instance
        }
    }

    private fun setPlayState(state: Boolean) {
        // When state is true -> the media is not playing
        if (state) {
            btnPlay.setImageDrawable(context?.getDrawable(android.R.drawable.ic_media_play))

        } else {
            btnPlay.setImageDrawable(context?.getDrawable(android.R.drawable.ic_media_pause))
        }
    }

    private fun updateProgress() {
        mLastPlaybackState?.let {
            var currentPos = it.position.toInt()
            Log.d(this::class.java.simpleName, "current pos = $currentPos")
            if (it.state == PlaybackStateCompat.STATE_PLAYING) {
                val timeDelta = SystemClock.elapsedRealtime() - it.lastPositionUpdateTime
                currentPos += (timeDelta * it.playbackSpeed).toInt()
            }
            seekBar.progress = currentPos
        }
    }

    private fun scheduleSeekbarUpdate() {
        stopSeekBarUpdate()
        if (!mExecutorService.isShutdown) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    { mHandler.post(mUpdateProgressTask) }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS)
        }
    }

    private fun stopSeekBarUpdate() {
        mScheduleFuture?.cancel(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSeekBarUpdate()
        mExecutorService.shutdown()
    }
}
