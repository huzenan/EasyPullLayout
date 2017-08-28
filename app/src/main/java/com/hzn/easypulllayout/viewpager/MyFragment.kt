package com.hzn.easypulllayout.viewpager

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hzn.easypulllayout.R
import kotlinx.android.synthetic.main.fragment_my.*

/**
 * MyFragment
 *
 * Created by huzenan on 2017/8/6.
 */
class MyFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_my, container, false)
        return view
    }

    override fun onResume() {
        super.onResume()
        iv.setImageResource(arguments.getInt("img_res"))
    }
}