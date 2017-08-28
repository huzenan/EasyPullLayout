package com.hzn.easypulllayout

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hzn.easypulllayout.nested.NestedActivity
import com.hzn.easypulllayout.fixed.FixedActivity
import com.hzn.easypulllayout.listview.ListViewActivity
import com.hzn.easypulllayout.recyclerview.RecyclerViewActivity
import com.hzn.easypulllayout.viewpager.ViewPagerActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnListView.setOnClickListener { startActivity(Intent(this, ListViewActivity::class.java)) }
        btnRecyclerView.setOnClickListener { startActivity(Intent(this, RecyclerViewActivity::class.java)) }
        btnViewPager.setOnClickListener { startActivity(Intent(this, ViewPagerActivity::class.java)) }
        btnFixed.setOnClickListener { startActivity(Intent(this, FixedActivity::class.java)) }
        btnAll.setOnClickListener { startActivity(Intent(this, NestedActivity::class.java)) }
    }
}
