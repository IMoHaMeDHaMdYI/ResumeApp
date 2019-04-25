package mohamed.mohamedresume.intro.ui.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import mohamed.mohamedresume.R
import mohamed.mohamedresume.intro.models.Way
import mohamed.mohamedresume.intro.ui.viewholders.WayViewHolder

class WayAdapter(
        private val context: Context,
        private val wayList: ArrayList<Way<out AppCompatActivity>>,
        private val onClick: (activity: Class<out AppCompatActivity>) -> Unit
) : BaseAdapter<WayViewHolder, Way<out AppCompatActivity>>(context, wayList) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WayViewHolder {
        Log.d(this::class.java.simpleName, viewType.toString())
        return WayViewHolder(LayoutInflater.from(context).inflate(R.layout.item_way, parent, false))
    }

    override fun onBindViewHolder(holder: WayViewHolder, position: Int) {
        val way = wayList[position]
        Log.d(this::class.java.simpleName,"Reached here")
        holder.apply {
            tvWayName.text = way.skillName
            itemView.setOnClickListener {
                onClick(way.activity)
            }
        }

    }
}