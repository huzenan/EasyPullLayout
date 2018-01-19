package com.hzn.lib

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator

/**
 * Pull layout that support both VERTICAL and HORIZONTAL
 *
 * Created by huzenan on 2017/8/3.
 */
class EasyPullLayout : ViewGroup {

    private var trigger_offset_left = 0
    private var trigger_offset_top = 0
    private var trigger_offset_right = 0
    private var trigger_offset_bottom = 0
    private var max_offset_left = 0
    private var max_offset_top = 0
    private var max_offset_right = 0
    private var max_offset_bottom = 0
    private var fixed_content_left = false
    private var fixed_content_top = false
    private var fixed_content_right = false
    private var fixed_content_bottom = false
    private var roll_back_duration = 0L // default 300
    private var auto_refresh_rolling_duration = 0L // default 300
    private var sticky_factor = 0f // default 0.66f (0f~1f)

    private var childViews = HashMap<View, ChildViewAttr>(4)

    private var downX = 0f
    private var downY = 0f
    private var offsetX = 0f
    private var offsetY = 0f
    private var lastPullFraction = 0f

    private var currentType: Int? = TYPE_NONE
    private var currentState: Int? = STATE_IDLE

    private var horizontalAnimator: ValueAnimator? = null
    private var verticalAnimator: ValueAnimator? = null

    // listener returning the edge type
    private var onEdgeListener: () -> Int? = { TYPE_NONE }
    // listener while triggered
    private var onPullListener: ((type: Int?, fraction: Float?, changed: Boolean) -> Unit)? = null
    // listener while triggered
    private var onTriggerListener: ((type: Int?) -> Unit)? = null

    companion object {
        const val TYPE_NONE = -1 // not being controlled by EasyPullLayout
        const val TYPE_EDGE_LEFT = 0
        const val TYPE_EDGE_TOP = 1
        const val TYPE_EDGE_RIGHT = 2
        const val TYPE_EDGE_BOTTOM = 3
        const val TYPE_CONTENT = 4

        const val STATE_IDLE = 0
        const val STATE_ROLLING = 1
        const val STATE_TRIGGERING = 2
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        context?.theme?.obtainStyledAttributes(attrs, R.styleable.EasyPullLayout, defStyleAttr, 0).let {
            trigger_offset_left = it?.getDimensionPixelOffset(R.styleable.EasyPullLayout_trigger_offset_left,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -1f, context?.resources?.displayMetrics).toInt())!!
            trigger_offset_top = it.getDimensionPixelOffset(R.styleable.EasyPullLayout_trigger_offset_top,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -1f, context?.resources?.displayMetrics).toInt())
            trigger_offset_right = it.getDimensionPixelOffset(R.styleable.EasyPullLayout_trigger_offset_right,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -1f, context?.resources?.displayMetrics).toInt())
            trigger_offset_bottom = it.getDimensionPixelOffset(R.styleable.EasyPullLayout_trigger_offset_bottom,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -1f, context?.resources?.displayMetrics).toInt())
            max_offset_left = it.getDimensionPixelOffset(R.styleable.EasyPullLayout_max_offset_left,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -1f, context?.resources?.displayMetrics).toInt())
            max_offset_top = it.getDimensionPixelOffset(R.styleable.EasyPullLayout_max_offset_top,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -1f, context?.resources?.displayMetrics).toInt())
            max_offset_right = it.getDimensionPixelOffset(R.styleable.EasyPullLayout_max_offset_right,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -1f, context?.resources?.displayMetrics).toInt())
            max_offset_bottom = it.getDimensionPixelOffset(R.styleable.EasyPullLayout_max_offset_bottom,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -1f, context?.resources?.displayMetrics).toInt())
            fixed_content_left = it.getBoolean(R.styleable.EasyPullLayout_fixed_content_left, false)
            fixed_content_top = it.getBoolean(R.styleable.EasyPullLayout_fixed_content_top, false)
            fixed_content_right = it.getBoolean(R.styleable.EasyPullLayout_fixed_content_right, false)
            fixed_content_bottom = it.getBoolean(R.styleable.EasyPullLayout_fixed_content_bottom, false)
            roll_back_duration = it.getInteger(R.styleable.EasyPullLayout_roll_back_duration, 300).toLong()
            auto_refresh_rolling_duration = it.getInteger(R.styleable.EasyPullLayout_auto_refresh_rolling_duration, 300).toLong()
            sticky_factor = it.getFloat(R.styleable.EasyPullLayout_sticky_factor, 0.66f)
            sticky_factor = if (sticky_factor < 0f) 0f else if (sticky_factor > 1f) 1f else sticky_factor // limit 0f~1f
            it.recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        var i = 0
        while (i < childCount) {
            getChildAt(i++).let {
                val lp = it.layoutParams as LayoutParams
                childViews.getByType(lp.type)?.let {
                    throw Exception("Each child type can only be defined once!")
                } ?: childViews.put(it, ChildViewAttr())
            }
        }
        val contentView = childViews.getByType(TYPE_CONTENT) ?:
                throw Exception("Child type \"content\" must be defined!")
        setOnEdgeListener {
            childViews.getByType(TYPE_EDGE_LEFT)?.let {
                if (!contentView.canScrollHorizontally(-1))
                    return@setOnEdgeListener TYPE_EDGE_LEFT
            }
            childViews.getByType(TYPE_EDGE_RIGHT)?.let {
                if (!contentView.canScrollHorizontally(1))
                    return@setOnEdgeListener TYPE_EDGE_RIGHT
            }
            childViews.getByType(TYPE_EDGE_TOP)?.let {
                if (!contentView.canScrollVertically(-1))
                    return@setOnEdgeListener TYPE_EDGE_TOP
            }
            childViews.getByType(TYPE_EDGE_BOTTOM)?.let {
                if (!contentView.canScrollVertically(1))
                    return@setOnEdgeListener TYPE_EDGE_BOTTOM
            }
            TYPE_NONE
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        for ((childView, childViewAttr) in childViews) {
            measureChildWithMargins(childView, widthMeasureSpec, 0, heightMeasureSpec, 0)
            val lp = childView.layoutParams as LayoutParams
            when (lp.type) {
                TYPE_EDGE_LEFT, TYPE_EDGE_RIGHT -> {
                    childViewAttr.size = childView.measuredWidth + lp.leftMargin + lp.rightMargin
                    trigger_offset_left = if (trigger_offset_left < 0) childViewAttr.size / 2 else trigger_offset_left
                    trigger_offset_right = if (trigger_offset_right < 0) childViewAttr.size / 2 else trigger_offset_right
                    max_offset_left = if (max_offset_left < 0) childViewAttr.size else max_offset_left
                    max_offset_right = if (max_offset_right < 0) childViewAttr.size else max_offset_right
                }
                TYPE_EDGE_TOP, TYPE_EDGE_BOTTOM -> {
                    childViewAttr.size = childView.measuredHeight + lp.topMargin + lp.bottomMargin
                    trigger_offset_top = if (trigger_offset_top < 0) childViewAttr.size / 2 else trigger_offset_top
                    trigger_offset_bottom = if (trigger_offset_bottom < 0) childViewAttr.size / 2 else trigger_offset_bottom
                    max_offset_top = if (max_offset_top < 0) childViewAttr.size else max_offset_top
                    max_offset_bottom = if (max_offset_bottom < 0) childViewAttr.size else max_offset_bottom
                }
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val contentView = childViews.getByType(TYPE_CONTENT)
        val contentWidth = contentView?.measuredWidth
                ?: throw Exception("EasyPullLayout must have and only have one layout_type \"content\"!")
        val contentHeight = contentView.measuredHeight

        for ((childView, childViewAttr) in childViews) {
            val lp = childView.layoutParams as LayoutParams
            var left: Int = paddingLeft + lp.leftMargin
            var top: Int = paddingTop + lp.topMargin
            var right: Int = left + childView.measuredWidth
            var bottom: Int = top + childView.measuredHeight
            when (lp.type) {
                TYPE_EDGE_LEFT -> {
                    left -= childViewAttr.size
                    right -= childViewAttr.size
                }
                TYPE_EDGE_TOP -> {
                    top -= childViewAttr.size
                    bottom -= childViewAttr.size
                }
                TYPE_EDGE_RIGHT -> {
                    left += contentWidth
                    right += contentWidth
                }
                TYPE_EDGE_BOTTOM -> {
                    top += contentHeight
                    bottom += contentHeight
                }
            }
            childViewAttr.set(left, top, right, bottom) // child views' initial location
            childView.layout(left, top, right, bottom)
        }
        if (fixed_content_left) childViews.getByType(TYPE_EDGE_LEFT)?.bringToFront()
        if (fixed_content_top) childViews.getByType(TYPE_EDGE_TOP)?.bringToFront()
        if (fixed_content_right) childViews.getByType(TYPE_EDGE_RIGHT)?.bringToFront()
        if (fixed_content_bottom) childViews.getByType(TYPE_EDGE_BOTTOM)?.bringToFront()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (currentState != STATE_IDLE)
            return false

        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                val type = onEdgeListener.invoke()
                val dx = ev.x - downX
                val dy = ev.y - downY
                currentType = type
                return when (type) {
                    TYPE_EDGE_LEFT -> ev.x > downX && Math.abs(dx) > Math.abs(dy)
                    TYPE_EDGE_RIGHT -> ev.x < downX && Math.abs(dx) > Math.abs(dy)
                    TYPE_EDGE_TOP -> ev.y > downY && Math.abs(dy) > Math.abs(dx)
                    TYPE_EDGE_BOTTOM -> ev.y < downY && Math.abs(dy) > Math.abs(dx)
                    TYPE_NONE -> false
                    else -> false
                }
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (currentState != STATE_IDLE)
            return false

        parent.requestDisallowInterceptTouchEvent(true)

        when (event?.action) {
            MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y
                offsetX = (x - downX) * (1 - sticky_factor * 0.75f)
                offsetY = (y - downY) * (1 - sticky_factor * 0.75f)
                move()
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                currentState = STATE_ROLLING
                when (currentType) {
                    TYPE_EDGE_LEFT, TYPE_EDGE_RIGHT -> rollBackHorizontal()
                    TYPE_EDGE_TOP, TYPE_EDGE_BOTTOM -> rollBackVertical()
                }
            }
        }
        return true
    }

    private fun move() {
        var pullFraction = 0f

        // limit the offset
        when (currentType) {
            TYPE_EDGE_LEFT -> {
                offsetX = if (offsetX < 0) 0f else if (offsetX > max_offset_left) max_offset_left.toFloat() else offsetX
                pullFraction = if (offsetX == 0f) 0f else if (trigger_offset_left > offsetX) offsetX / trigger_offset_left else 1f
            }
            TYPE_EDGE_RIGHT -> {
                offsetX = if (offsetX > 0) 0f else if (offsetX < -max_offset_right) -max_offset_right.toFloat() else offsetX
                pullFraction = if (offsetX == 0f) 0f else if (-trigger_offset_right < offsetX) offsetX / -trigger_offset_right else 1f
            }
            TYPE_EDGE_TOP -> {
                offsetY = if (offsetY < 0) 0f else if (offsetY > max_offset_top) max_offset_top.toFloat() else offsetY
                pullFraction = if (offsetY == 0f) 0f else if (trigger_offset_top > offsetY) offsetY / trigger_offset_top else 1f
            }
            TYPE_EDGE_BOTTOM -> {
                offsetY = if (offsetY > 0) 0f else if (offsetY < -max_offset_bottom) -max_offset_bottom.toFloat() else offsetY
                pullFraction = if (offsetY == 0f) 0f else if (-trigger_offset_bottom < offsetY) offsetY / -trigger_offset_bottom else 1f
            }
        }
        val changed = !(lastPullFraction < 1f && pullFraction < 1f || lastPullFraction == 1f && pullFraction == 1f)
        onPullListener?.invoke(currentType, pullFraction, changed)
        lastPullFraction = pullFraction

        when (currentType) {
            TYPE_EDGE_LEFT ->
                for ((childView, childViewAttr) in childViews)
                    if ((childView.layoutParams as LayoutParams).type != TYPE_CONTENT || !fixed_content_left)
                        childView.x = childViewAttr.left + offsetX
            TYPE_EDGE_RIGHT ->
                for ((childView, childViewAttr) in childViews)
                    if ((childView.layoutParams as LayoutParams).type != TYPE_CONTENT || !fixed_content_right)
                        childView.x = childViewAttr.left + offsetX
            TYPE_EDGE_TOP ->
                for ((childView, childViewAttr) in childViews)
                    if ((childView.layoutParams as LayoutParams).type != TYPE_CONTENT || !fixed_content_top)
                        childView.y = childViewAttr.top + offsetY
            TYPE_EDGE_BOTTOM ->
                for ((childView, childViewAttr) in childViews)
                    if ((childView.layoutParams as LayoutParams).type != TYPE_CONTENT || !fixed_content_bottom)
                        childView.y = childViewAttr.top + offsetY
        }
    }

    private fun rollBackHorizontal() {
        val rollBackOffset =
                if (offsetX > trigger_offset_left) offsetX - trigger_offset_left
                else if (offsetX < -trigger_offset_right) offsetX + trigger_offset_right
                else offsetX
        val triggerOffset =
                if (rollBackOffset != offsetX) {
                    when (currentType) {
                        TYPE_EDGE_LEFT -> trigger_offset_left
                        TYPE_EDGE_RIGHT -> -trigger_offset_right
                        else -> 0
                    }
                } else 0
        horizontalAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = roll_back_duration
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                when (currentType) {
                    TYPE_EDGE_LEFT ->
                        for ((childView, childViewAttr) in childViews)
                            if ((childView.layoutParams as LayoutParams).type != TYPE_CONTENT || !fixed_content_left)
                                childView.x = childViewAttr.left + triggerOffset + rollBackOffset * animatedValue as Float
                    TYPE_EDGE_RIGHT ->
                        for ((childView, childViewAttr) in childViews)
                            if ((childView.layoutParams as LayoutParams).type != TYPE_CONTENT || !fixed_content_right)
                                childView.x = childViewAttr.left + triggerOffset + rollBackOffset * animatedValue as Float
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    if (triggerOffset != 0 && currentState == STATE_ROLLING) {
                        currentState = STATE_TRIGGERING
                        offsetX = triggerOffset.toFloat()
                        onTriggerListener?.invoke(currentType)
                    } else {
                        currentState = STATE_IDLE
                        offsetX = 0f
                    }
                }
            })
            start()
        }
    }

    private fun rollBackVertical() {
        val rollBackOffset =
                if (offsetY > trigger_offset_top) offsetY - trigger_offset_top
                else if (offsetY < -trigger_offset_bottom) offsetY + trigger_offset_bottom
                else offsetY
        val triggerOffset =
                if (rollBackOffset != offsetY) {
                    when (currentType) {
                        TYPE_EDGE_TOP -> trigger_offset_top
                        TYPE_EDGE_BOTTOM -> -trigger_offset_bottom
                        else -> 0
                    }
                } else 0
        verticalAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = roll_back_duration
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                when (currentType) {
                    TYPE_EDGE_TOP ->
                        for ((childView, childViewAttr) in childViews)
                            if ((childView.layoutParams as LayoutParams).type != TYPE_CONTENT || !fixed_content_top)
                                childView.y = childViewAttr.top + triggerOffset + rollBackOffset * animatedValue as Float
                    TYPE_EDGE_BOTTOM ->
                        for ((childView, childViewAttr) in childViews)
                            if ((childView.layoutParams as LayoutParams).type != TYPE_CONTENT || !fixed_content_bottom)
                                childView.y = childViewAttr.top + triggerOffset + rollBackOffset * animatedValue as Float
                }

            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    if (triggerOffset != 0 && currentState == STATE_ROLLING) {
                        currentState = STATE_TRIGGERING
                        offsetY = triggerOffset.toFloat()
                        onTriggerListener?.invoke(currentType)
                    } else {
                        currentState = STATE_IDLE
                        offsetY = 0f
                    }
                }
            })
            start()
        }
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return null != p && p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return LayoutParams(p)
    }

    /**
     * stop triggering
     */
    fun stop() {
        when (currentType) {
            TYPE_EDGE_LEFT, TYPE_EDGE_RIGHT -> rollBackHorizontal()
            TYPE_EDGE_TOP, TYPE_EDGE_BOTTOM -> rollBackVertical()
        }
    }

    /**
     * Start refresh automatically.
     */
    fun autoRefresh(typeEdge: Int) {
        if (currentState != STATE_IDLE)
            return

        if (typeEdge != TYPE_EDGE_LEFT &&
                typeEdge != TYPE_EDGE_TOP &&
                typeEdge != TYPE_EDGE_RIGHT &&
                typeEdge != TYPE_EDGE_BOTTOM)
            return

        currentState = STATE_ROLLING
        currentType = typeEdge

        val end = when (currentType) {
            TYPE_EDGE_LEFT -> max_offset_left
            TYPE_EDGE_TOP -> max_offset_top
            TYPE_EDGE_RIGHT -> -max_offset_right
            TYPE_EDGE_BOTTOM -> -max_offset_bottom
            else -> 0
        }.toFloat()

        verticalAnimator = ValueAnimator.ofFloat(0f, end).apply {
            duration = auto_refresh_rolling_duration
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                when (currentType) {
                    TYPE_EDGE_LEFT, TYPE_EDGE_RIGHT -> offsetX = animatedValue as Float
                    TYPE_EDGE_TOP, TYPE_EDGE_BOTTOM -> offsetY = animatedValue as Float
                }
                move()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    when (currentType) {
                        TYPE_EDGE_LEFT, TYPE_EDGE_RIGHT -> rollBackHorizontal()
                        TYPE_EDGE_TOP, TYPE_EDGE_BOTTOM -> rollBackVertical()
                    }
                }
            })
            start()
        }
    }

    /**
     * set the listener which to trigger the pulling, return the edge type if content view is on edge
     */
    fun setOnEdgeListener(l: () -> Int?) {
        this.onEdgeListener = l
    }

    /**
     * set the listener, called when pulling, fraction represents the offset percent,
     * from 0f to 1f, 1f means max trigger offset
     */
    fun setOnPullListener(l: (type: Int?, fraction: Float?, changed: Boolean) -> Unit) {
        this.onPullListener = l
    }

    /**
     * set the listener, called when triggered
     */
    fun setOnTriggerListener(l: (type: Int?) -> Unit) {
        this.onTriggerListener = l
    }

    class LayoutParams : ViewGroup.MarginLayoutParams {
        var type = TYPE_NONE

        constructor(c: Context?, attrs: AttributeSet?) : super(c, attrs) {
            c?.theme?.obtainStyledAttributes(attrs, R.styleable.EasyPullLayout_LayoutParams, 0, 0).let {
                type = it?.getInt(R.styleable.EasyPullLayout_LayoutParams_layout_type, TYPE_NONE)!!
                it.recycle()
            }
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: MarginLayoutParams?) : super(source)
        constructor(source: ViewGroup.LayoutParams?) : super(source)
    }

    class ChildViewAttr(
            var left: Int = 0,
            var top: Int = 0,
            var right: Int = 0,
            var bottom: Int = 0,
            var size: Int = 0) {
        fun set(left: Int = 0,
                top: Int = 0,
                right: Int = 0,
                bottom: Int = 0,
                size: Int = 0) {
            this.left = left
            this.top = top
            this.right = right
            this.bottom = bottom
            this.size = size
        }
    }
}