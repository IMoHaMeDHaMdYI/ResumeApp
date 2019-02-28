package mohamed.mohamedresume.imageeditor.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.customview.widget.ViewDragHelper
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.activity_image_browser.*
import mohamed.mohamedresume.R
import mohamed.mohamedresume.hardcodeddata.IMAGE_DIRECTORY
import mohamed.mohamedresume.imageeditor.models.Image
import mohamed.mohamedresume.imageeditor.ui.ImageAdapter.Companion.MODE_BROWSE
import mohamed.mohamedresume.imageeditor.ui.ImageAdapter.Companion.MODE_EDIT
import java.io.File

class ImageBrowserActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName
    private lateinit var imageAdapter: ImageAdapter

    private var mode = MODE_BROWSE
    private val mImageList = ArrayList<Image>()
    private lateinit var mDragHelper: ViewDragHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_browser)

        // initialize the drag helper to help sliding the fragment
//        mDragHelper = ViewDragHelper.create(frameFragment,1.0f,DragHelperCallback())


        btnAdd.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
        btnDelete.setOnClickListener {
            if (mode == MODE_EDIT) {
                imageAdapter.mCheckedImages.forEach { image ->
                    image.delete { error ->
                        Toast.makeText(this, "Deleting Image Failed.", Toast.LENGTH_LONG).show()
                        Log.e(TAG, "The exception says : ${error.message}\n${error.localizedMessage}")
                    }
                    imageAdapter.delete(image)
                }
                // Update the imageAdapter to delete the images from the view.
            }
        }
        getImages()
        imageAdapter = ImageAdapter(this, mImageList) { action, position, image ->
            when (action) {
                ImageAdapter.ACTION_CLICK_IMG -> {
                    if (mode == MODE_BROWSE) {
                        openFullScreen(position)
                    } else {
                        image.selected = !image.selected
                        // this line should change the foreground of the image checked
                        imageAdapter.notifyItemChanged(position)
                    }
                }
                ImageAdapter.ACTION_LONG_CLICK_IMG -> {
                    if (mode == MODE_BROWSE) {
                        mode = MODE_EDIT
                        //TODO update UI to be in the edit mode
                        switchMode()
                        imageAdapter.setMode(mode)
                        imageAdapter.notifyDataSetChanged()
                    }
                }
                else -> {
                }
            }
        }
        rv.adapter = imageAdapter
        rv.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        frameFragment.setOnDismissListener {
            closeFullScreen()
        }
        frameFragment.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (frameFragment.isMoving()) {
                v.top = oldTop
                v.bottom = oldBottom
                v.left = oldLeft
                v.right = oldRight
            }
        }
    }

    @SuppressLint("RestrictedApi")
    // make the view pager visible and navigate to the image that was clicked
    private fun openFullScreen(pos: Int) {
        val fragment = FullScreenFragment.create(pos, mImageList)
        frameFragment.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameFragment, fragment, "full screen")
            .show(fragment)
            .commit()
    }

    private fun closeFullScreen() {
        frameFragment.visibility = View.INVISIBLE
        supportFragmentManager.findFragmentByTag("full screen")?.let {
            supportFragmentManager.beginTransaction()
                .hide(it)
                .remove(it)
                .commit()
        }
    }

    //    private fun getImages(): List<Image>? {
//        val file = getImageDirectory()
//        file?.let { f ->
//            return f.listFiles { dir, name ->
//                Log.d(TAG, "$dir")
//                name.substringAfterLast(".") == "jpg"
//            }.map {
//                Image(it.path)
//            }
//        }
//        return null
//    }
    private fun getImages() {
        val file = getImageDirectory()
        file?.let { f ->
            return f.listFiles { dir, name ->
                Log.d(TAG, "$dir")
                name.substringAfterLast(".") == "jpg"
            }.forEach {
                mImageList.add(Image(it.path))
            }
        }
    }

    private fun getImageDirectory(): File? {
        try {
            val file = File("${Environment.getExternalStorageDirectory()}/$IMAGE_DIRECTORY")
            if (!file.exists())
                file.mkdir()
            return file
        } catch (e: SecurityException) {
            needPermissions()
        }
        return null
    }

    private val REQUEST_STORAGE_PERMISSION = 666

    private fun needPermissions(): Boolean {
        val permissionArray = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE).filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionArray.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionArray.toTypedArray(), REQUEST_STORAGE_PERMISSION)
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            imageAdapter.clear()
            getImages()
            imageAdapter.add(mImageList)
        }
    }

    override fun onBackPressed() {
        Log.d(TAG, "here ${supportFragmentManager.fragments.size}")

        if (supportFragmentManager.fragments.size > 1)
            closeFullScreen()
        else if (imageAdapter.getMode() == MODE_EDIT) {
            imageAdapter.setMode(MODE_BROWSE)
            mode = MODE_BROWSE
            switchMode()
        } else
            super.onBackPressed()
    }

    @SuppressLint("RestrictedApi")
    private fun switchMode() {
        // the rotation degree describes if the animation is forward or backward
        val rotation = if (btnAdd.rotation == 360f) 0f else 360f
        // duration for the animation
        val d = 600L
        btnAdd.animate().rotation(rotation).apply {
            duration = d
            interpolator = FastOutSlowInInterpolator()
            withStartAction {
                //animate the delete button
                btnDelete.animate().rotation(rotation).apply {
                    duration = d
                    start()
                    interpolator = FastOutSlowInInterpolator()
                }
            }
            val addVisibility = btnAdd.switchedVisibility()
            val deleteVisibility = btnDelete.switchedVisibility()
            var changed = false
            setUpdateListener {
                // if the delete button is visible hide it and if it's invisible show it
                // the same happens to the add button
                if (it.animatedValue as Float >= 0.5 && !changed) {
                    btnAdd.visibility = addVisibility
                    btnDelete.visibility = deleteVisibility
                    changed = true
                }
            }
            start()
        }
    }

    // Used to switch the visibility of the buttons when changing the mode between edit and browse
    private fun View.switchedVisibility(): Int {
        return if (visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
    }
}
