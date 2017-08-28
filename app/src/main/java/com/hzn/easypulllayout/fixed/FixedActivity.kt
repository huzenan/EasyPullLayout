package com.hzn.easypulllayout.fixed

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Toast
import com.hzn.easypulllayout.R
import kotlinx.android.synthetic.main.activity_fixed.*
import java.util.*

class FixedActivity : AppCompatActivity() {

    companion object {
        const val REFRESHING_TIME = 3000L
    }

    private var isRefreshing = false
    private var random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fixed)
        init()
    }

    private fun init() {
        val list = (1..30).map { "item_string $it" }
        lv.adapter = ArrayAdapter<String>(this, R.layout.item_string, R.id.tv, list)

        epl.setOnTriggerListener {
            launchRocket()
            simulateLoading()
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

    private fun simulateLoading() {
        Handler().postDelayed({
            Toast.makeText(this, "finish", Toast.LENGTH_SHORT).show()
            isRefreshing = false
            epl.stop()
        }, REFRESHING_TIME)
    }
}
