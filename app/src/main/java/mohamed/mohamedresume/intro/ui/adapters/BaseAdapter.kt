package mohamed.mohamedresume.intro.ui.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<VH : RecyclerView.ViewHolder, D>(
    private val context: Context
    , private val dataList: ArrayList<D>
) : RecyclerView.Adapter<VH>() {
    val HOLDER_LOADING = 0
    val HOLDER = 1
    val HOLDER_NOT_FOUND_ERROR = 2
    val HOLDER_UNKNOWN_ERROR = 3
    private var loading = false
    override fun getItemCount(): Int {
        return dataList.size + if (loading) 1 else 0
    }

    fun add(data: D) {
        if (this.dataList.size == 0 && this.itemCount == 1) notifyItemRemoved(0)
        dataList.add(data)
        notifyItemInserted(dataList.size - 1)
    }

    fun add(data: D, at: Int) {
        if (this.dataList.size == 0 && this.itemCount == 1) notifyItemRemoved(0)
        dataList.add(at, data)
        notifyItemInserted(at)
    }

    fun add(dataList: ArrayList<D>) {
        if (this.dataList.size == 0 && this.itemCount == 1) notifyItemRemoved(0)
        val lstSz = this.dataList.size
        this.dataList.addAll(dataList)
        notifyItemRangeInserted(lstSz, dataList.size)
    }

    fun replace(data: D, at: Int) {
        try {
            dataList[at] = data
            notifyItemChanged(at)
        } catch (e: IndexOutOfBoundsException) {
            add(data)
        }

    }

    fun delete(data: D): Boolean {
        if (dataList.remove(data)) {
            notifyDataSetChanged()
            return true
        }
        return false
    }


    fun change(nwDataList: ArrayList<D>) {
        this.dataList.clear()
        this.dataList.addAll(nwDataList)
        notifyDataSetChanged()
    }

    fun clear() {
        this.dataList.clear()
        notifyDataSetChanged()
    }

    fun setLoading(state: Boolean = true) {
        loading = state
        if (loading)
            notifyItemInserted(itemCount)
        else
            notifyItemRemoved(itemCount - 1)
    }

    override fun getItemViewType(position: Int): Int {
        return when (loading) {
            true -> HOLDER_LOADING
            else -> HOLDER
        }
    }
}