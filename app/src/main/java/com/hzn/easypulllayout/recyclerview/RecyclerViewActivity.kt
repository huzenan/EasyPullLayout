package com.hzn.easypulllayout.recyclerview

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.hzn.easypulllayout.R
import com.hzn.easypulllayout.RvAdapter
import com.hzn.lib.EasyPullLayout
import kotlinx.android.synthetic.main.activity_recyclerview.*

class RecyclerViewActivity : AppCompatActivity() {

    companion object {
        const val START_FRACTION = 0.5f
        const val REFRESHING_TIME = 5000L
    }

    var list: List<String> = (1..30).map { "item_string $it" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)
        initRecyclerView()
        initEasyPullLayout()
    }

    private fun initRecyclerView() {
        rv.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        rv.adapter = RvAdapter(this, list)
    }

    private fun initEasyPullLayout() {
        topView.type = TransformerView.TYPE_TOP
        bottomView.type = TransformerView.TYPE_BOTTOM

        epl.setOnPullListener { type, fraction, changed ->
            when (type) {
                EasyPullLayout.TYPE_EDGE_TOP -> {
                    topView.setFraction(START_FRACTION, fraction)
                    if (fraction == 1f)
                        topView.ready()
                    else
                        topView.idle()
                }
                EasyPullLayout.TYPE_EDGE_BOTTOM -> {
                    bottomView.setFraction(START_FRACTION, fraction)
                    if (fraction == 1f)
                        bottomView.ready()
                    else
                        bottomView.idle()
                }
            }
        }
        epl.setOnTriggerListener {
            when (it) {
                EasyPullLayout.TYPE_EDGE_TOP -> {
                    topView.triggered()
                }
                EasyPullLayout.TYPE_EDGE_BOTTOM -> {
                    bottomView.triggered()
                }
            }
            simulateLoading()
        }
    }

    private fun simulateLoading() {
        Handler().postDelayed({
            Toast.makeText(this, getString(R.string.finish), Toast.LENGTH_SHORT).show()
            epl.stop()
            topView.stop()
            bottomView.stop()
        }, REFRESHING_TIME)
    }
}
