package mohamed.mohamedresume.imageeditor.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.utils.ThumbnailItem
import kotlinx.android.synthetic.main.item_filter.view.*
import mohamed.mohamedresume.R
import mohamed.mohamedresume.intro.ui.adapters.BaseAdapter

class ImageFilterAdapter(
    private val context: Context, private val thumbnailList: ArrayList<ThumbnailItem>,
    private val onFilterSelected: (Filter) -> Unit
) : BaseAdapter<ImageFilterAdapter.ImageFilterViewHolder, ThumbnailItem>(context, thumbnailList) {

    private var mSelectedIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageFilterViewHolder {
        return ImageFilterViewHolder(LayoutInflater.from(context).inflate(R.layout.item_filter, parent, false))
    }

    override fun onBindViewHolder(holder: ImageFilterViewHolder, position: Int) {
        val thumbnail = thumbnailList[position]
        holder.imgFilter?.apply {
            setImageBitmap(thumbnail.image)
            setOnClickListener {
                onFilterSelected(thumbnail.filter)
                mSelectedIndex = position
                notifyItemChanged(position)
            }
            if (mSelectedIndex == position) {
                //TODO change the appearance of the thumbnail so you can know it's the chosen
            } else {
                //TODO change the appearance so the thumbnail is not clicked
            }
        }
    }

    class ImageFilterViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val imgFilter = view.imgFilter ?: null
    }
}