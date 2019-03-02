package mohamed.mohamedresume.imageeditor.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.item_full_screen.view.*
import mohamed.mohamedresume.R
import mohamed.mohamedresume.imageeditor.models.Image
import mohamed.mohamedresume.extensions.GlideApp

class FullScreenAdapter(private val context: Context, private val imageList: ArrayList<Image>) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.item_full_screen, container, false)
        GlideApp.with(context)
            .load(imageList[position].path)
            .fitCenter()
            .into(view.img)
        container.addView(view)
        return view

    }

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    override fun getCount() = imageList.size

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    fun getImagePath(pos: Int): String {
        return imageList[pos].path
    }

}