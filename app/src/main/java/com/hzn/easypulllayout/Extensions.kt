package com.hzn.easypulllayout

import android.util.DisplayMetrics
import android.view.View

/**
 * Extensions
 *
 * Created by huzenan on 2017/8/6.
 */
internal fun View.dpToPx(dp: Int): Int {
    val displayMetrics = this.resources.displayMetrics
    return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
}