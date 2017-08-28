package com.hzn.easypulllayout.listview

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Toast
import com.hzn.easypulllayout.R
import com.hzn.lib.EasyPullLayout
import kotlinx.android.synthetic.main.activity_listview.*

class ListViewActivity : AppCompatActivity() {

    companion object {
        const val REFRESHING_TIME = 3000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listview)
        init()
    }

    private fun init() {
        val list = (1..30).map { "item_string $it" }
        lv.adapter = ArrayAdapter<String>(this, R.layout.item_string, R.id.tv, list)

        topView.setTop()
        bottomView.setBottom()

        epl.setOnPullListener { type, fraction, changed ->
            if (!changed)
                return@setOnPullListener

            when (type) {
                EasyPullLayout.TYPE_EDGE_TOP -> {
                    if (fraction == 1f)
                        topView.ready()
                    else
                        topView.idle()
                }
                EasyPullLayout.TYPE_EDGE_BOTTOM -> {
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
            Toast.makeText(this, "finish", Toast.LENGTH_SHORT).show()
            epl.stop()
        }, REFRESHING_TIME)
    }
}