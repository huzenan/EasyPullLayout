package com.hzn.easypulllayout.listview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.LinearLayout
import com.hzn.easypulllayout.R
import com.hzn.easypulllayout.dpToPx
import kotlinx.android.synthetic.main.view_top_and_bottom.view.*

/**
 * Top and bottom View
 *
 * Created by huzenan on 2017/8/6.
 */
class TopAndBottomView : LinearLayout {

    companion object {
        const val ANIMATION_DURATION = 300L
    }

    private var toRotationReady = 180f
    private var toRotationIdle = 0f

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(getContext()).inflate(R.layout.view_top_and_bottom, this, true)
    }

    fun setTop() {
        toRotationReady = 180f
        toRotationIdle = 0f
        (tv.layoutParams as MarginLayoutParams).topMargin = dpToPx(75)
        (ivArrow.layoutParams as MarginLayoutParams).topMargin = dpToPx(75)
        (pbLoading.layoutParams as MarginLayoutParams).topMargin = dpToPx(70)
        idle()
    }

    fun setBottom() {
        toRotationReady = 0f
        toRotationIdle = 180f
        (tv.layoutParams as MarginLayoutParams).topMargin = dpToPx(15)
        (ivArrow.layoutParams as MarginLayoutParams).topMargin = dpToPx(15)
        (pbLoading.layoutParams as MarginLayoutParams).topMargin = dpToPx(10)
        idle()
    }

    fun idle() {
        tv.text = context.getString(R.string.idle)
        pbLoading.visibility = View.GONE
        ivArrow.visibility = View.VISIBLE
        ivArrow.animate()
                .rotation(toRotationIdle)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(BounceInterpolator())
                .start()
    }

    fun ready() {
        tv.text = context.getString(R.string.ready)
        pbLoading.visibility = View.GONE
        ivArrow.visibility = View.VISIBLE
        ivArrow.animate()
                .rotation(toRotationReady)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(BounceInterpolator())
                .start()
    }

    fun triggered() {
        tv.text = context.getString(R.string.triggered)
        pbLoading.visibility = View.VISIBLE
        ivArrow.visibility = View.GONE
    }
}