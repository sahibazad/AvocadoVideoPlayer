package com.sahib.avocado.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.sahib.avocado.R
import com.sahib.avocado.adapter.ViewPagerAdapter
import com.sahib.avocado.utils.startActivityWithFade
import customcomponents.ViewpagerHeader


class OnboardingActivity : AppCompatActivity() {

    lateinit var pager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        initView()
    }

    private fun initView() {
        val viewPagerHeader = findViewById<ViewpagerHeader>(R.id.motionLayout)
        pager = findViewById(R.id.pager)

        val adapter = ViewPagerAdapter(supportFragmentManager, 3)
        pager.adapter = adapter
        if (viewPagerHeader != null) {
            pager.addOnPageChangeListener(viewPagerHeader)
        }

//        val debugMode = if (intent.getBooleanExtra("showPaths", false)) {
//            MotionLayout.DEBUG_SHOW_PATH
//        } else {
//            MotionLayout.DEBUG_SHOW_NONE
//        }
//        viewPagerHeader.setDebugMode(debugMode)
    }

    fun finishOnboarding() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityWithFade(intent)
        TODO(reason = "Implement shared preferences")
    }
}