package com.hzn.easypulllayout.nested

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.hzn.easypulllayout.R
import com.hzn.easypulllayout.viewpager.DetailActivity
import com.hzn.lib.EasyPullLayout
import kotlinx.android.synthetic.main.activity_nested.*
import java.util.*

class NestedActivity : AppCompatActivity() {

    companion object {
        const val REFRESHING_TIME = 3000L
    }

    private var isRefreshing = false
    private var random = Random()

    var list: List<Int> = listOf(R.mipmap.androido, R.mipmap.androidn, R.mipmap.androidm, R.mipmap.androidl)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nested)
        initRecyclerView()
        initEpl()
    }

    private fun initRecyclerView() {
        rvH1.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }
        rvH1.adapter = MyAdapter(this, list)

        rvH2.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }
        rvH2.adapter = MyAdapter(this, list)
    }

    private fun initEpl() {
        bottomView.setBottom()
        epl.setOnPullListener { type, fraction, changed ->
            if (!changed || type != EasyPullLayout.TYPE_EDGE_BOTTOM)
                return@setOnPullListener

            if (fraction == 1f)
                bottomView.ready()
            else
                bottomView.idle()
        }
        epl.setOnTriggerListener {
            when (it) {
                EasyPullLayout.TYPE_EDGE_TOP -> {
                    launchRocket()
                    simulateLoading(epl)
                }
                EasyPullLayout.TYPE_EDGE_BOTTOM -> {
                    bottomView.triggered()
                    simulateLoading(epl)
                }
            }
        }

        rightView.setRight()
        epl1.setOnPullListener { type, fraction, changed ->
            if (!changed)
                return@setOnPullListener

            if (fraction == 1f)
                rightView.ready(getString(R.string.release))
            else
                rightView.idle(getString(R.string.pull))
        }
        epl1.setOnTriggerListener {
            rightView.triggered(getString(R.string.wait))
            simulateLoading(epl1)
        }

        leftView.setLeft()
        epl2.setOnPullListener { type, fraction, changed ->
            if (!changed)
                return@setOnPullListener

            if (fraction == 1f)
                leftView.ready()
            else
                leftView.idle()
        }
        epl2.setOnTriggerListener {
            leftView.triggered()
            startActivity(Intent(this, DetailActivity::class.java))
            epl2.stop()
        }
    }

    private fun launchRocket() {
        isRefreshing = true
        Thread({
            while (isRefreshing) {
                Thread.sleep(30L)
                topView.x = random.nextFloat() * 10 - 5
                topView.y = random.nextFloat() * 10 - 5
            }
        }).start()
    }

    private fun simulateLoading(epl: EasyPullLayout) {
        Handler().postDelayed({
            Toast.makeText(this, "finish", Toast.LENGTH_SHORT).show()
            if (epl.id == R.id.epl)
                isRefreshing = false
            epl.stop()
        }, REFRESHING_TIME)
    }

    class MyAdapter(var context: Context, var list: List<Any>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            holder?.iv?.setImageResource(list[position] as Int)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var iv: ImageView? = null

            init {
                iv = itemView.findViewById(R.id.iv) as ImageView?
            }
        }
    }
}
