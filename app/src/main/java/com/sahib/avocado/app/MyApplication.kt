package com.sahib.avocado.app

import android.app.Application
import com.sahib.avocado.utils.GeneralSharedPreferences

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        prefHelper = GeneralSharedPreferences(this)
    }

    companion object {
        lateinit var prefHelper: GeneralSharedPreferences
    }
}