package com.wyz.common.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import com.wyz.common.utils.DensityUtils

class CircularProgressView : AppCompatImageView {

    private var mStroke = 5
    private var mProcess = 0
    private var mTotal = 100
    private val mNormalColor = -0x1
    private val mSecondColor = -0x11d00
    private val mStartAngle = -90
    private var mRectF: RectF? = null

    private var mPaint: Paint
    private var mDrawable: Drawable? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        mStroke = DensityUtils.dp2px(context, mStroke.toFloat())
        mPaint = Paint()
        mPaint.setColor(mNormalColor)
        mPaint.setStrokeWidth(mStroke.toFloat())
        mPaint.setStyle(Paint.Style.STROKE)
        mPaint.setAntiAlias(true)
        mDrawable = Progress()
        setImageDrawable(mDrawable)
    }

    fun setTotal(total: Int) {
        mTotal = total
        mDrawable?.invalidateSelf()
    }

    fun setProcess(process: Int) {
        mProcess = process
        post { mDrawable?.invalidateSelf() }
    }
    fun getProcess(): Int {
        return mProcess
    }

    fun setStroke(dp: Float) {
        mStroke = DensityUtils.dp2px(context, dp)
        mPaint.strokeWidth = mStroke.toFloat()
        mDrawable?.invalidateSelf()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec)
        } else {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        }
    }


    private inner class Progress : Drawable() {
        override fun draw(canvas: Canvas) {
            val width: Int = getWidth()
            val pd: Int = mStroke / 2 + 1
            if (mRectF == null) {
                mRectF = RectF(pd.toFloat(), pd.toFloat(), (width - pd).toFloat(), (width - pd).toFloat())
            }
            mPaint.setStyle(Paint.Style.STROKE)
            mPaint.setColor(mNormalColor)
            canvas.drawCircle(width / 2.toFloat(), width / 2.toFloat(), width / 2 - pd.toFloat(), mPaint)
            mPaint.setColor(mSecondColor)
            canvas.drawArc(mRectF!!, mStartAngle.toFloat(), mProcess * 360 / mTotal .toFloat(), false, mPaint)
        }

        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: ColorFilter?) {}
        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }
    }
}