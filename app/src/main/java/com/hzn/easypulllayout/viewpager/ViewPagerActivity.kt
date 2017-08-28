package com.hzn.easypulllayout.viewpager

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import com.hzn.easypulllayout.R
import com.hzn.lib.EasyPullLayout
import kotlinx.android.synthetic.main.activity_view_pager.*

class ViewPagerActivity : AppCompatActivity() {

    private val resList = listOf(R.mipmap.androido, R.mipmap.androidn, R.mipmap.androidm)
    val fragmentList = arrayListOf<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pager)
        initViewPager()
        initEasyPullLayout()
    }

    private fun initViewPager() {
        for (i in 0..2) {
            MyFragment().apply {
                val args = Bundle()
                args.putInt("img_res", resList[i])
                arguments = args
                fragmentList.add(this)
            }
        }
        vp.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int) = fragmentList[position]
            override fun getCount() = fragmentList.size
        }
    }

    private fun initEasyPullLayout() {
        leftView.setLeft()
        rightView.setRight()

        epl.setOnPullListener { type, fraction, changed ->
            if (!changed)
                return@setOnPullListener

            when (type) {
                EasyPullLayout.TYPE_EDGE_LEFT -> {
                    if (fraction == 1f)
                        leftView.ready()
                    else
                        leftView.idle()
                }
                EasyPullLayout.TYPE_EDGE_RIGHT -> {
                    if (fraction == 1f)
                        rightView.ready()
                    else
                        rightView.idle()
                }
            }
        }
        epl.setOnTriggerListener {
            when (it) {
                EasyPullLayout.TYPE_EDGE_LEFT -> {
                    leftView.triggered()
                }
                EasyPullLayout.TYPE_EDGE_RIGHT -> {
                    rightView.triggered()
                }
            }
            triggered()
        }
    }

    private fun triggered() {
        epl.stop()
        startActivity(Intent(this, DetailActivity::class.java))
    }
}
