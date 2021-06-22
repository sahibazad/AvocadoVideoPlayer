package com.sahib.avocado.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sahib.avocado.Constants
import com.sahib.avocado.model.VideoFolderContent

class DirectoryViewModel : ViewModel() {

    private val directoryList = MutableLiveData<ArrayList<VideoFolderContent>>()

    lateinit var sharedPreferences: SharedPreferences
    lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    open fun instantiateSharePref(context: Context) {
        sharedPreferences = context.getSharedPreferences(
            Constants.SharedPrefNames.directories.name,
            Context.MODE_PRIVATE
        )

        sharedPreferencesEditor = sharedPreferences.edit()
    }

    open fun getDefaultDirectoryList(): ArrayList<VideoFolderContent> {
        val directoryListString = sharedPreferences.getString(Constants.SharedPrefItemNames.list.name, "")

        if (!directoryListString.isNullOrEmpty()) {
            val itemType = object : TypeToken<ArrayList<VideoFolderContent>>() {}.type
            return Gson().fromJson(directoryListString, itemType)
        } else {
            return ArrayList()
        }
    }

    open fun updateDirectoryList(updatedDirectoryList: ArrayList<VideoFolderContent>) {
        val directoryListString = Gson().toJson(updatedDirectoryList)

        sharedPreferencesEditor.putString(Constants.SharedPrefItemNames.list.name, directoryListString).commit()

        directoryList.value = updatedDirectoryList
    }
}
