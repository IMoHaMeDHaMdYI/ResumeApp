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

class CameraHelper(private val mActivity: Activity, private val mTextureView: TextureView,
                   private val onImageAvailable: (ByteArray) -> Unit) {
    private lateinit var mCameraDevice: CameraDevice
    private lateinit var mCameraId: String
    private lateinit var mCameraCaptureSession: CameraCaptureSession
    private lateinit var mCaptureRequestBuilder: CaptureRequest.Builder
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

    private var mCaptureCallback: CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            super.onCaptureCompleted(session, request, result)
            createCameraPreview(mImageDimension.width, mImageDimension.height)
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
            mCameraDevice.close()
            mCameraOpen = false
        }

        override fun onError(camera: CameraDevice, error: Int) {
            mCameraDevice.close()
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

    //
    @SuppressLint("MissingPermission")
    @RequiresPermission(android.Manifest.permission.CAMERA)
    fun openCamera() {
        val manager = mActivity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            mCameraId = manager.cameraIdList[0]
            val cameraChars = manager.getCameraCharacteristics(mCameraId)
            cameraChars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.let { map ->
                mImageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
                manager.openCamera(mCameraId, mStateCallback, null)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun closeCamera() {
        if (mCameraOpen) {
            mCameraDevice.close()
            mImageReader?.close()
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
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                mCaptureRequestBuilder.addTarget(surface)
                mCameraDevice.createCaptureSession(
                    listOf(surface).toMutableList(),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigureFailed(session: CameraCaptureSession) {

                        }

                        override fun onConfigured(session: CameraCaptureSession) {
                            mCameraCaptureSession = session
                            updatePreview()
                        }

                    },
                    null
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
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try {
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            onCameraAccessException(e)
        }
    }

    fun takePicture(mxHeight: Int = 640, mxWidth: Int = 480) {
        try {
            val cameraChars = mCameraManager.getCameraCharacteristics(mCameraDevice.id)
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
            val outputSurfaces = ArrayList<Surface>().apply {
                add(mImageReader!!.surface)
                add(Surface(mTextureView.surfaceTexture))
            }
            val captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(mImageReader!!.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            val rotation = mActivity.windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))
            mImageReader!!.setOnImageAvailableListener(mReaderListener, mBackgroundHandler)
            mCameraDevice.createCaptureSession(outputSurfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.d(TAG, "Configuration failed.")
                }

                override fun onConfigured(session: CameraCaptureSession) {
                    Log.d(TAG, "Configured. ")
                    try {
                        session.capture(captureBuilder.build(), mCaptureCallback, mBackgroundHandler)
                    } catch (e: CameraAccessException) {
                        onCameraAccessException(e)
                    }
                }
            }, mBackgroundHandler)

        } catch (e: CameraAccessException) {
            onCameraAccessException(e)
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
}