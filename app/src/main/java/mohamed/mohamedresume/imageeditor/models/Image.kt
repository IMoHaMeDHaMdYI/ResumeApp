package mohamed.mohamedresume.imageeditor.models

import android.util.Log
import java.io.File

data class Image(val path: String, val data: String = "")
    : Checkable {
    override fun onCheck() {
        selected = !selected
    }

    var selected = false
    fun delete(onDeleteFail: (e: SecurityException) -> Unit = { e -> }) {
        val file = File(path)
        try {
            if (file.delete())
                Log.d("Image", "DELETED")
        } catch (e: SecurityException) {
            onDeleteFail(e)
        }
    }
}