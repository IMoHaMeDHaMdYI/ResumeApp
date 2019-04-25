package mohamed.mohamedresume.imageeditor

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import androidx.annotation.RequiresPermission
import java.io.FileNotFoundException
import java.io.IOException

class CameraHelper(
    private val mActivity: Activity, private val mTextureView: TextureView,
    private val onImageAvailable: (ByteArray) -> Unit
) {


    private var mCameraDevice: CameraDevice? = null
    private lateinit var mCameraId: String
    private var mCameraCaptureSession: CameraCaptureSession? = null
    private lateinit var mCaptureRequestBuilder: CaptureRequest.Builder
    private lateinit var mPreviewRequest: CaptureRequest
    private lateinit var mImageDimension: Size
    private var mImageReader: ImageReader? = null
    private lateinit var mBackgroundHandler: Handler
    private lateinit var mBackgroundThread: HandlerThread
    private val TAG = this::class.java.simpleName
    private var mStarted = false
    private var mCameraOpen = false
    private var onCameraAccessException: (CameraAccessException) -> Unit = {}

    private val ORIENTATIONS = SparseIntArray().apply {
        append(Surface.ROTATION_0, 90)
        append(Surface.ROTATION_90, 0)
        append(Surface.ROTATION_180, 270)
        append(Surface.ROTATION_270, 180)
    }
    private val mCameraManager = mActivity.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private var mCaptureCallback: CameraCaptureSession.CaptureCallback =
        object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                super.onCaptureCompleted(session, request, result)
            }
        }

    //TODO add states and configure each of them.
    private fun runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mCaptureRequestBuilder.set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START
            )
            // Tell #captureCallback to wait for the precapture sequence to be set.
            state = STATE_WAITING_PRECAPTURE
            mCameraCaptureSession?.capture(
                mCaptureRequestBuilder.build(), mCaptureCallback,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    fun setCaptureCallback(captureCallback: CameraCaptureSession.CaptureCallback) {
        mCaptureCallback = captureCallback
    }

    private var mTextureViewListener: TextureView.SurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) = Unit

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?) = false
        @SuppressLint("MissingPermission")
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            openCamera()
        }
    }

    fun setTextureViewListener(textureViewListener: TextureView.SurfaceTextureListener) {
        mTextureViewListener = textureViewListener

    }

    private var mStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            mCameraDevice = camera
            mCameraOpen = true
            createCameraPreview(mImageDimension.width, mImageDimension.height)
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            mCameraOpen = false
            mCameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            onDisconnected(camera)
            mCameraOpen = false
            when (error) {
                CameraDevice.StateCallback.ERROR_CAMERA_DEVICE ->
                    Log.d(TAG, "{CameraDevice.StateCallback.ERROR_CAMERA_DEVICE}")
                CameraDevice.StateCallback.ERROR_CAMERA_DISABLED ->
                    Log.d(TAG, "{CameraDevice.StateCallback.ERROR_CAMERA_DISABLED}")
                CameraDevice.StateCallback.ERROR_CAMERA_IN_USE ->
                    Log.d(TAG, "{CameraDevice.StateCallback.ERROR_CAMERA_IN_USE}")
                CameraDevice.StateCallback.ERROR_CAMERA_SERVICE ->
                    Log.d(TAG, "{CameraDevice.StateCallback.ERROR_CAMERA_SERVICE}")
                CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE ->
                    Log.d(TAG, "CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE")
            }
        }
    }

    fun setStateCallback(stateCallback: CameraDevice.StateCallback) {
        mStateCallback = stateCallback
    }

    private var mReaderListener: ImageReader.OnImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        mBackgroundHandler.post {
            var img: Image? = null
            Log.d(TAG, "reader Listener")
            try {
                img = reader.acquireLatestImage()
                val byteBuffer = img.planes[0].buffer
                val bytes = ByteArray(byteBuffer.capacity())
                byteBuffer.get(bytes)
                onImageAvailable(bytes)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                img?.close()
            }
        }
    }

    //
    @SuppressLint("MissingPermission")
    @RequiresPermission(android.Manifest.permission.CAMERA)
    fun openCamera() {
        val manager = mActivity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            mCameraId = manager.cameraIdList[0]
            val cameraChars = manager.getCameraCharacteristics(mCameraId)
            setUpCamera()
            cameraChars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.let { map ->
                mImageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
                manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun closeCamera() {
        try {
            mCameraCaptureSession?.close()
            mCameraCaptureSession = null
            mCameraDevice?.close()
            mCameraDevice = null
            mImageReader?.close()
            mImageReader = null
        } catch (e: InterruptedException) {
            Log.d("Error", "Interrupt Error ${e.localizedMessage}")
        } finally {

        }
        mCameraOpen = false
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Thread").also { it.start() }
        mBackgroundHandler = Handler(mBackgroundThread.looper)
    }

    private fun stopBackgroundThread() {
        mBackgroundThread.quitSafely()
    }

    fun createCameraPreview(width: Int, height: Int) {
        try {
            mTextureView.surfaceTexture?.let { texture ->
                texture.setDefaultBufferSize(width, height)
                val surface = Surface(texture)
                mCaptureRequestBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                mCaptureRequestBuilder.addTarget(surface)
                mCameraDevice?.createCaptureSession(
                    listOf(surface, mImageReader?.surface).toMutableList(),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigureFailed(session: CameraCaptureSession) {

                        }

                        override fun onConfigured(session: CameraCaptureSession) {
                            if (mCameraDevice == null) return
                            mCameraCaptureSession = session
                            updatePreview()
                        }

                    },
                    mBackgroundHandler
                )
            }
        } catch (e: CameraAccessException) {
            onCameraAccessException(e)
        }
    }

    fun setOnCameraAccessException(onCameraAccessException: (CameraAccessException) -> Unit) {
        this.onCameraAccessException = onCameraAccessException
    }

    private fun updatePreview() {
        try {
            mCaptureRequestBuilder.set(
                CaptureRequest.CONTROL_AF_MODE,
                CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            mPreviewRequest = mCaptureRequestBuilder.build()
            mCameraCaptureSession!!.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            onCameraAccessException(e)
        }
    }

    private fun setUpCamera(mxHeight: Int = 640, mxWidth: Int = 480) {
        try {
            val cameraChars = mCameraManager.getCameraCharacteristics(mCameraId)
            val jpegSizes = cameraChars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                ?.getOutputSizes(ImageFormat.JPEG)
            var height = mxHeight
            var width = mxWidth
            jpegSizes?.let {
                if (!it.isEmpty()) {
                    width = it[0].width
                    height = it[0].height
                }
            }
            mImageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
            mImageReader!!.setOnImageAvailableListener(mReaderListener, mBackgroundHandler)

        } catch (e: CameraAccessException) {
            onCameraAccessException(e)
        }
    }

    fun takePicture() {
        try {
            val captureBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(mImageReader!!.surface)
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            val rotation = mActivity.windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))
            val captureCallback = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    unlockFocus()
                }
            }
            mCameraCaptureSession?.apply {
                stopRepeating()
                abortCaptures()
                capture(captureBuilder.build(), captureCallback, null)
            }

        } catch (e: CameraAccessException) {
            onCameraAccessException(e)
        }
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private fun unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mCaptureRequestBuilder.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_CANCEL
            )
            mCameraCaptureSession?.capture(
                mCaptureRequestBuilder.build(), mCaptureCallback,
                mBackgroundHandler
            )
            // After this, the camera will go back to the normal state of preview.
            state = STATE_PREVIEW
            mCameraCaptureSession?.setRepeatingRequest(
                mPreviewRequest, mCaptureCallback,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.CAMERA)
    fun start() {
        startBackgroundThread()
        if (mTextureView.isAvailable)
            openCamera()
        else
            mTextureView.surfaceTextureListener = mTextureViewListener
        mStarted = true
    }

    fun stop() {
        if (mStarted) {
            closeCamera()
            stopBackgroundThread()
        }
        mStarted = false
    }

    /**
     * Camera state: Showing camera preview.
     */
    private val STATE_PREVIEW = 0

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private val STATE_WAITING_LOCK = 1

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private val STATE_WAITING_PRECAPTURE = 2

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private val STATE_WAITING_NON_PRECAPTURE = 3

    /**
     * Camera state: Picture was taken.
     */
    private val STATE_PICTURE_TAKEN = 4


    private var state = STATE_PREVIEW

}