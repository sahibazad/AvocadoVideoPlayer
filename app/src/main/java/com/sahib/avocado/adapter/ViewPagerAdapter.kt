package com.sahib.avocado.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.sahib.avocado.ui.fragments.OnboardingFragment1
import com.sahib.avocado.ui.fragments.OnboardingFragment2
import com.sahib.avocado.ui.fragments.OnboardingFragment3

class ViewPagerAdapter(fm: FragmentManager, numTabs: Int) : FragmentPagerAdapter(fm, numTabs) {

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return OnboardingFragment1()
            }
            1 -> {
                return OnboardingFragment2()
            }
            else -> {
                return OnboardingFragment3()
            }
        }
    }

    override fun getCount(): Int {
        return 3
    }
}
