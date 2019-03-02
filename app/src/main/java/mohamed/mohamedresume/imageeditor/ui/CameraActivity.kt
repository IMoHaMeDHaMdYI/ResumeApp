package mohamed.mohamedresume.imageeditor.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_camera.*
import mohamed.mohamedresume.R
import mohamed.mohamedresume.hardcodeddata.IMAGE_DIRECTORY
import mohamed.mohamedresume.imageeditor.CameraHelper
import mohamed.mohamedresume.extensions.GlideApp
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*


//TODO Encrypt images before saving...
class CameraActivity : AppCompatActivity() {

    private val TAG = this::class.java.simpleName

    private fun save(bytes: ByteArray) {
        var output: OutputStream? = null
        val directory = Environment.getExternalStorageDirectory()
        val folder = File("$directory/$IMAGE_DIRECTORY")
        if (!folder.exists()) {
            folder.mkdir()
        }
        val file = File("$folder/${UUID.randomUUID()}.jpg")
        Log.d(TAG, file.toString())
        try {
            output = FileOutputStream(file)
            output.write(bytes)
            runOnUiThread {
                GlideApp.with(this)
                    .load(file.path)
                    .into(imgPreview)
            }
        } finally {
            output?.close()
        }
    }

    private val REQUEST_CAMERA_PERMISSION: Int = 555

    private fun needPermissions(): Boolean {
        val permissionArray = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE).filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionArray.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionArray.toTypedArray(), REQUEST_CAMERA_PERMISSION)
            return true
        }
        return false
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            grantResults.forEach {
                if (it == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Please Accept the permission so we can open the camera", Toast.LENGTH_LONG)
                        .show()
                    return
                }
            }
            cameraHelper.openCamera()
        }
    }


    private lateinit var cameraHelper: CameraHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        cameraHelper = CameraHelper(this, textureView) { save(it) }
        btnCapture.setOnClickListener { cameraHelper.takePicture() }

    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        if (!needPermissions())
            cameraHelper.start()
    }

    override fun onPause() {
        super.onPause()
        cameraHelper.stop()
    }



}
