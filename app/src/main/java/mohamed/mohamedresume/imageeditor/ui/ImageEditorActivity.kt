package mohamed.mohamedresume.imageeditor.ui

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.LinearLayout.HORIZONTAL
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.zomato.photofilters.FilterPack
import com.zomato.photofilters.utils.ThumbnailItem
import com.zomato.photofilters.utils.ThumbnailsManager
import kotlinx.android.synthetic.main.activity_image_editor.*
import mohamed.mohamedresume.R
import mohamed.mohamedresume.utils.GlideApp
import java.nio.file.Files.delete
import java.nio.channels.FileChannel.MapMode.READ_WRITE
import java.io.File.separator
import android.os.Environment.getExternalStorageDirectory
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import android.media.ExifInterface




class ImageEditorActivity : AppCompatActivity() {

    private val thumbnailItemList = ArrayList<ThumbnailItem>()
    private lateinit var filterAdapter: ImageFilterAdapter
    private lateinit var mPath: String
    private var mBitmap: Bitmap? = null
    private val TAG = this::class.java.simpleName


    init {
        System.loadLibrary("NativeImageProcessor")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_editor)
        receiveImage()


        filterAdapter = ImageFilterAdapter(this, thumbnailItemList) {
            mBitmap?.let { bitmap ->
                try {
                    val exif = ExifInterface(mPath)
                    val bit = Bitmap.createBitmap(bitmap)
                    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED)
                    val filteredImage = rotateBitmap(bit,orientation) //bitmap.copy(Bitmap.Config.ARGB_8888,true)
                    Log.d(TAG, "$bitmap")
                    Log.d(TAG, "$filteredImage")

                    imgEdit.setImageBitmap(it.processFilter(filteredImage))
                } catch (e:IOException){

                }
            }
        }
        mBitmap = getImageBitmap(mPath)
        mBitmap?.let {
            Log.d(TAG, "bitmap is not null")
            prepareThumbnail(it)
        }
        Log.d(TAG, "after null")

        rvFilters.apply {
            adapter = filterAdapter
            layoutManager = LinearLayoutManager(this@ImageEditorActivity, HORIZONTAL, false)
        }
    }

    private fun receiveImage() {
        intent?.getStringExtra("file_name")?.let {
            mPath = it
            GlideApp.with(this)
                .load(it)
                .into(imgEdit)
        }
    }

    private fun getImageBitmap(path: String): Bitmap? {
        return try {
            val bitmap = BitmapFactory.decodeFile(path)
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "error", e)
            null
        }
    }

    private fun prepareThumbnail(bitmap: Bitmap) {
        val r = Runnable {
            val exif = ExifInterface(mPath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED)
            val thumbImage = rotateBitmap(bitmap,orientation) ?: return@Runnable
            ThumbnailsManager.clearThumbs()
            thumbnailItemList.clear()

            // add normal bitmap first
            val thumbnailItem = ThumbnailItem()
            thumbnailItem.image = thumbImage
            thumbnailItem.filterName = getString(R.string.filter_normal)
            ThumbnailsManager.addThumb(thumbnailItem)

            val filters = FilterPack.getFilterPack(this)

            for (filter in filters) {
                val tI = ThumbnailItem()
                tI.image = thumbImage
                tI.filter = filter
                tI.filterName = filter.name
                ThumbnailsManager.addThumb(tI)
            }

            thumbnailItemList.addAll(ThumbnailsManager.processThumbs(this))
            Log.d(TAG, "thumbnails : $thumbnailItemList")
            runOnUiThread {
                filterAdapter.notifyDataSetChanged()
            }
        }
        Thread(r).start()
    }


    private fun pxToDp(px: Int): Int {
        return (px / Resources.getSystem().displayMetrics.density).toInt()
    }

    fun convertToMutable(imgIn: Bitmap): Bitmap {
        var imgIn = imgIn
        try {
            // get the width and height of the source bitmap.
            val width = imgIn.width
            val height = imgIn.height
            val type = imgIn.config

            imgIn = Bitmap.createBitmap(width, height, type)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return imgIn
    }

    fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap? {

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> return bitmap
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90F)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90F)
                matrix.postScale(-1F, 1F)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90F)
            else -> return bitmap
        }
        return try {
            val bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//            bitmap.recycle()
            bmRotated
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            null
        } catch (e:OutOfMemoryError) {
            e.printStackTrace()
            null
        }

    }
}
