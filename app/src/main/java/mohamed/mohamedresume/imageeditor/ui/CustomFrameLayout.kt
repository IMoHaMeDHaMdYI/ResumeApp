package mohamed.mohamedresume.imageeditor.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper


class CustomFrameLayout(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {
    var mDragHelper = ViewDragHelper.create(this, 1.0f, DragHelperCallback())
    private val AUTO_OPEN_SPEED_LIMIT = 800.0
    private var mDragBorder = 0
    private var mVerticalRange = 0
    private var mIsOpen = false
    private var mDragState = ViewDragHelper.STATE_IDLE
    private var onDismiss = {}

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
//        when(ev.action){
//            MotionEvent.ACTION_DOWN -> Log.d("class","action down")
//            MotionEvent.ACTION_MOVE -> Log.d("class","action move")
//            MotionEvent.ACTION_UP -> Log.d("class","action up")
//            else-> Log.d("class","else")
//        }
        if (visibility == View.VISIBLE && mDragHelper.shouldInterceptTouchEvent(ev))
            return true
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (visibility == View.VISIBLE) {
            mDragHelper.processTouchEvent(event)
            return true
        }
        return false
    }

    inner class DragHelperCallback :
        ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return true
        }


        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            mDragBorder = top
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return mVerticalRange
        }


        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return (y + top).toInt()
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {

            return (x + left).toInt()

        }



        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)
            if (state == mDragState)
                return
            if (mDragState == ViewDragHelper.STATE_DRAGGING || mDragState == ViewDragHelper.STATE_SETTLING
                && state == ViewDragHelper.STATE_IDLE) {
                // The view stopped moving
                if (mDragBorder == 0) {
                    onStopDraggingToClosed()
                } else if (mDragBorder == mVerticalRange) {
                    mIsOpen = true
                }
            }
            if (state == ViewDragHelper.STATE_DRAGGING)
                onStartDragging()
            mDragState = state
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val rangeToCheck = mVerticalRange
            if (mDragBorder == 0) {
                mIsOpen = false
                return
            }
            if (mDragBorder == rangeToCheck) {
                mIsOpen = true
                return
            }
            var settleToOpen = false
            if (yvel > AUTO_OPEN_SPEED_LIMIT) { // speed has priority over position
                settleToOpen = true
            } else if (yvel < -AUTO_OPEN_SPEED_LIMIT) {
                settleToOpen = false
            } else if (mDragBorder > rangeToCheck / 2) {
                settleToOpen = true
            } else if (mDragBorder < rangeToCheck / 2) {
                settleToOpen = false
            }

            if (settleToOpen) {
                onDismiss()
            }

//            val settleDestY = if (settleToOpen) mVerticalRange else 0
//
            if (mDragHelper.settleCapturedViewAt(0, 0)) {
                ViewCompat.postInvalidateOnAnimation(this@CustomFrameLayout)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mVerticalRange = (h * 0.66).toInt()
        Log.d("class","something")
        super.onSizeChanged(w, h, oldw, oldh)
    }

    fun onStartDragging() {

    }

    fun onStopDraggingToClosed() {

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mDragHelper = ViewDragHelper.create(this, 1.0f, DragHelperCallback())
    }

    override fun computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    fun isMoving() = mDragState == ViewDragHelper.STATE_DRAGGING || mDragState == ViewDragHelper.STATE_SETTLING

    fun isOpen() = mIsOpen

    fun setOnDismissListener(onDismissListener: () -> Unit) {
        onDismiss = onDismissListener
    }
}