package mohamed.mohamedresume.intro.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_home.*
import mohamed.mohamedresume.R
import mohamed.mohamedresume.hardcodeddata.Trip
import mohamed.mohamedresume.intro.ui.adapters.WayAdapter
import mohamed.mohamedresume.utils.wait

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        btnYes.isEnabled = false
        btnNo.isEnabled = false

        tv.animateText(Trip.questionList[Trip.currentIndex]) {
            Log.d("yes", "done")
            btnYes.isEnabled = true
            btnNo.isEnabled = true
        }
        btnNo.setOnClickListener {
            Trip.currentIndex = Trip.no
            proceed()
        }
        btnYes.setOnClickListener {
            Trip.currentIndex = Trip.yes
            proceed()
        }

        rv.adapter = WayAdapter(this, Trip.wayList) {
            startActivity(Intent(this, it))
        }
        rv.layoutManager = LinearLayoutManager(this)
    }

    private fun proceed() {
        btnNo.visibility = View.INVISIBLE
        btnYes.visibility = View.INVISIBLE
        tv.animateText(Trip.questionList[Trip.currentIndex]) {
            wait(300, {
                tv.animateText(Trip.questionList[Trip.cont]) {
                    Trip.currentIndex = 0
                    motionLayout.transitionToEnd()
                    btnNo.visibility = View.INVISIBLE
                    btnYes.visibility = View.INVISIBLE
                }
            })
        }
    }


}
