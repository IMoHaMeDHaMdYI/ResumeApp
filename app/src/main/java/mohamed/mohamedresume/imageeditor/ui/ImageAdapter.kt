package mohamed.mohamedresume.imageeditor.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_image.view.*
import mohamed.mohamedresume.R
import mohamed.mohamedresume.imageeditor.models.Image
import mohamed.mohamedresume.intro.ui.adapters.BaseAdapter
import mohamed.mohamedresume.extensions.GlideApp

class ImageAdapter(private val context: Context, private val dataList: ArrayList<Image>,
                   private val onAction: (action: Int, position: Int, image: Image) -> Unit
                   = { _, _, _ -> }) :
    BaseAdapter<ImageAdapter.ImageViewHolder, Image>(context, dataList) {
    private var mMode = MODE_BROWSE
    val mCheckedImages = ArrayList<Image>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image, parent, false))
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.img?.apply {
            val image = dataList[position]
            GlideApp.with(context)
                .load(image.path)
                .error(android.R.drawable.stat_notify_error)
                .centerCrop()
                .into(this)
            setOnLongClickListener {
                onAction(ACTION_LONG_CLICK_IMG, position, dataList[position])
                return@setOnLongClickListener true
            }
            setOnClickListener {
                onAction(ACTION_CLICK_IMG, position, dataList[position])
            }
        }
        holder.checkbox?.let {
            val image = dataList[position]
            it.setOnCheckedChangeListener { buttonView, isChecked ->
                image.selected = isChecked
                if (isChecked) mCheckedImages.add(image)
                else mCheckedImages.remove(image)
            }
            it.isChecked = image.selected
            if (mMode == MODE_BROWSE) {
                it.isChecked = false
                it.visibility = View.INVISIBLE
                image.selected = false
            } else {
                it.visibility = View.VISIBLE
            }
        }
    }

    fun setMode(mode: Int) {
        mMode = mode
        if (mMode == MODE_BROWSE){
            notifyDataSetChanged()
            mCheckedImages.clear()
        }
    }

    fun getMode(): Int {
        return mMode
    }

    class ImageViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val img = view.img ?: null
        val checkbox = view.checkbox ?: null
    }

    companion object {
        val MODE_EDIT = 0
        val MODE_BROWSE = 1
        val ACTION_LONG_CLICK_IMG = 1
        val ACTION_CLICK_IMG = 2
    }
}