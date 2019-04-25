package mohamed.mohamedresume.extensions

import androidx.appcompat.app.AppCompatActivity
import java.util.*

fun AppCompatActivity.wait(millis: Long, operateOnUi: () -> Unit = {}, operateOffUi: () -> Unit = {}) {
    Timer().apply {
        schedule(object : TimerTask() {
            override fun run() {
                operateOffUi()
                runOnUiThread {
                    operateOnUi()
                }
            }

        }, millis)
    }
}