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
class LeftAndRightView : LinearLayout {

    companion object {
        const val ANIMATION_DURATION = 300L
    }

    private var toRotationReady = 180f
    private var toRotationIdle = 0f

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(getContext()).inflate(R.layout.view_left_and_right, this, true)
    }

    fun setLeft() {
        toRotationReady = 0f
        toRotationIdle = 180f
        idle()
    }

    fun setRight() {
        toRotationReady = 180f
        toRotationIdle = 0f
        idle()
    }

    fun idle(text: String = context.getString(R.string.detail)) {
        tv.text = text
        ivArrow.animate()
                .rotation(toRotationIdle)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(BounceInterpolator())
                .start()
    }

    fun ready(text: String = context.getString(R.string.detail)) {
        tv.text = text
        ivArrow.animate()
                .rotation(toRotationReady)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(BounceInterpolator())
                .start()
    }

    fun triggered(text: String = context.getString(R.string.detail)) {
        tv.text = text
    }
}