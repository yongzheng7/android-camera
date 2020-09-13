package com.wyz.camera.utils

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager

class DensityUtils private constructor(){
    companion object{
        /**
         * dp转px
         */
        fun dp2px(context: Context, dpVal: Float): Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    dpVal, context.resources.displayMetrics).toInt()
        }

        /**
         * sp转px
         */
        fun sp2px(context: Context, spVal: Float): Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    spVal, context.resources.displayMetrics).toInt()
        }

        /**
         * px转dp
         */
        fun px2dp(context: Context, pxVal: Float): Float {
            val scale = context.resources.displayMetrics.density
            return pxVal / scale
        }

        /**
         * px转sp
         */
        fun px2sp(context: Context, pxVal: Float): Float {
            return pxVal / context.resources.displayMetrics.scaledDensity
        }

        /**
         * 获取屏幕宽度
         */
        fun getScreenWidth(context: Context): IntArray {
            var width = 0
            var height= 0 ;
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val dm = DisplayMetrics()
            wm.defaultDisplay.getMetrics(dm)
            width = dm.widthPixels
            height = dm.heightPixels
            return intArrayOf(width , height)
        }
    }
}
