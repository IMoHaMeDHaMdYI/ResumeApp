package mohamed.mohamedresume.imageeditor.ui


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_full_screen.*
import mohamed.mohamedresume.R
import mohamed.mohamedresume.imageeditor.models.Image


private var mImages = ArrayList<Image>()
private var mCurrentItem: Int = 0

class FullScreenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_full_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fullScreenAdapter = FullScreenAdapter(view.context, mImages)
        viewPager.adapter = fullScreenAdapter
        viewPager.offscreenPageLimit = 4
        Log.d(TAG, "here $mCurrentItem")
        viewPager.setCurrentItem(mCurrentItem, true)
        imgFilter.setOnClickListener {
            val intent = Intent(context, ImageEditorActivity::class.java)
            intent.putExtra("file_name", fullScreenAdapter.getImagePath(viewPager.currentItem))
            startActivity(intent)
        }
    }

    companion object {
        private val TAG = this::class.java.simpleName
        private val instance: FullScreenFragment by lazy {
            FullScreenFragment()
        }

        fun create(pos: Int, images: ArrayList<Image>): FullScreenFragment {
            mImages = images
            mCurrentItem = pos
            Log.d(TAG, "Current pos is $pos")
            return FullScreenFragment()
        }
    }

}
