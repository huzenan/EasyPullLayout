package com.hzn.easypulllayout.recyclerview

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.hzn.easypulllayout.R
import com.hzn.lib.EasyPathView
import kotlinx.android.synthetic.main.view_transformer.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Transformer View
 *
 * Created by huzenan on 2017/8/6.
 */
class TransformerView : LinearLayout {

    companion object {
        const val TYPE_TOP = 0
        const val TYPE_BOTTOM = 1
    }

    var type = TYPE_TOP
        set(value) {
            field = value
            when (type) {
                TYPE_TOP -> {
                    epvAutobots.visibility = View.VISIBLE
                    epvDecepticons.visibility = View.GONE
                }
                TYPE_BOTTOM -> {
                    epvAutobots.visibility = View.GONE
                    epvDecepticons.visibility = View.VISIBLE
                }
            }
        }

    private var date: String? = context.getString(R.string.refreshing_date)

    var animatorHandler = Handler()
    private val showDecepticonsRunnable: Runnable = Runnable {
        showDecepticons()
    }
    private val showAutobotsRunnable: Runnable = Runnable {
        showAutobots()
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(getContext()).inflate(R.layout.view_transformer, this, true)
    }

    fun idle() {
        tv.text = context.getString(R.string.idle)
        tvDate.text = date
    }

    fun ready() {
        tv.text = context.getString(R.string.ready)
    }

    fun triggered() {
        date = SimpleDateFormat("yyyy-MM-dd  hh:mm:ss", Locale.getDefault()).format(Date())
        tv.text = context.getString(R.string.triggered)
        tvDate.text = date
        when (type) {
            TYPE_TOP -> showDecepticons()
            TYPE_BOTTOM -> showAutobots()
        }
    }

    private fun showAutobots() {
        epvDecepticons.addOnAnimatorListener(object : EasyPathView.OnAnimatorListener() {
            override fun onAnimEnd(state: Int) {
                epvDecepticons.visibility = View.GONE
                epvAutobots.visibility = View.VISIBLE
                epvAutobots.addOnAnimatorListener(object : EasyPathView.OnAnimatorListener() {
                    override fun onAnimEnd(state: Int) {
                        animatorHandler.postDelayed(showDecepticonsRunnable, 500)
                    }
                })
                epvAutobots.startDraw()
            }
        })
        epvDecepticons.startErase()
    }

    private fun showDecepticons() {
        epvAutobots.addOnAnimatorListener(object : EasyPathView.OnAnimatorListener() {
            override fun onAnimEnd(state: Int) {
                epvAutobots.visibility = View.GONE
                epvDecepticons.visibility = View.VISIBLE
                epvDecepticons.addOnAnimatorListener(object : EasyPathView.OnAnimatorListener() {
                    override fun onAnimEnd(state: Int) {
                        animatorHandler.postDelayed(showAutobotsRunnable, 500)
                    }
                })
                epvDecepticons.startDraw()
            }
        })
        epvAutobots.startErase()
    }

    fun setFraction(startFraction: Float?, currentFraction: Float?) {
        if (null == startFraction || null == currentFraction)
            return

        val fraction = (currentFraction - startFraction) / (1 - startFraction)
        when (type) {
            TYPE_TOP -> {
                epvAutobots.setAnimProgress(fraction)
            }
            TYPE_BOTTOM -> {
                epvDecepticons.setAnimProgress(fraction)
            }
        }
    }

    fun stop() {
        epvAutobots.addOnAnimatorListener(null)
        epvAutobots.reset()
        epvDecepticons.addOnAnimatorListener(null)
        epvDecepticons.reset()
        animatorHandler.removeCallbacks(showAutobotsRunnable)
        animatorHandler.removeCallbacks(showDecepticonsRunnable)

        when (type) {
            TYPE_TOP -> {
                epvAutobots.visibility = View.VISIBLE
                epvDecepticons.visibility = View.GONE
                epvAutobots.startErase()
            }
            TYPE_BOTTOM -> {
                epvAutobots.visibility = View.GONE
                epvDecepticons.visibility = View.VISIBLE
                epvDecepticons.startErase()
            }
        }
    }
}