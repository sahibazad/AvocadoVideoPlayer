package com.sahib.avocado.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class GeneralSharedPreferences(private val context: Context) {

    fun defaultPrefs(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun customPrefs(name: String): SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }
}
