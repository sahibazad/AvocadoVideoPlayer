package com.sahib.avocado.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.sahib.avocado.R
import com.sahib.avocado.ui.activities.OnboardingActivity

class OnboardingFragment3 : Fragment() {

    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView =  inflater!!.inflate(R.layout.fragment_onboarding3, container, false)
        initView()
        return rootView
    }

    private fun initView() {
        rootView.findViewById<Button>(R.id.button_done).setOnClickListener(object : View.OnClickListener{

            override fun onClick(v: View?) {
                val parentActivity = activity as? OnboardingActivity
                parentActivity?.finishOnboarding()
            }

        })
    }

}