package com.sahib.avocado.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.viewpager.widget.ViewPager
import com.sahib.avocado.Constants
import com.sahib.avocado.R
import com.sahib.avocado.adapter.ViewPagerAdapter
import com.sahib.avocado.app.MyApplication
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
    }

    fun finishOnboarding() {
        MyApplication.prefHelper.defaultPrefs().edit {
            putBoolean(Constants.SharedPrefItemNames.isOnboarded.name, true).commit()
        }
        val intent = Intent(this, DirectoriesActivity::class.java)
        startActivityWithFade(intent)
        finish()
    }
}
