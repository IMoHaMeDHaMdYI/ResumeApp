package mohamed.mohamedresume.intro.ui.customviews

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import mohamed.mohamedresume.R


class TextAnimatorView(context: Context, attributeSet: AttributeSet) : TextView(context, attributeSet) {

    private var mText: CharSequence? = null
    private var mIndex = 0
    private var mDelay = 0L

    private var onProgress: (currentIndex: Int, lastIndex: Int, text: String) -> Unit = { c, i, t -> }
    private var onEnd: () -> Unit = {}

    private val mHandler = Handler()
    private val characterAdder: Runnable = object : Runnable {
        override fun run() {
            mText?.let {
                text = it.subSequence(0, mIndex++)

                if (mIndex <= it.length) {
                    onProgress(mIndex, it.length, text.toString())
                    mHandler.postDelayed(this, mDelay)
                } else onEnd()
            }
        }
    }

    fun animateText(text: CharSequence,onEnd: () -> Unit = {}) {
        this.onEnd = onEnd
        mText = text
        mIndex = 0
        setText("")
        mHandler.removeCallbacks(characterAdder)
        mHandler.postDelayed(characterAdder, mDelay)
    }

    fun setDelay(delay: Long) {
        mDelay = delay
    }

    init {
        Log.d("beeb", "First Constructor ")
        val styledAttr = context.obtainStyledAttributes(attributeSet, R.styleable.TextAnimatorView)
        val str = styledAttr.getString(R.styleable.TextAnimatorView_animateText)
        val delay = styledAttr.getInt(R.styleable.TextAnimatorView_setDelay, 10)
        setDelay(delay = delay.toLong())
        animateText(str ?: "")
        styledAttr.recycle()
    }

    fun setProgressListener(onProgress: (currentIndex: Int, lastIndex: Int, text: String) -> Unit = { c, i, t -> }
                            , onEnd: () -> Unit) {
        this.onProgress = onProgress
        this.onEnd = onEnd

    }

}