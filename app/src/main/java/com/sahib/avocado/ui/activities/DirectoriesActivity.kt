package com.sahib.avocado.ui.activities

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.sahib.avocado.R
import com.sahib.avocado.adapter.DirectoriesAdapter
import com.sahib.avocado.model.VideoFolderContent
import com.sahib.avocado.utils.VideoGet
import com.sahib.avocado.utils.checkFileExists
import com.sahib.avocado.viewmodel.DirectoryViewModel


class DirectoriesActivity : AppCompatActivity() {

    private lateinit var directoryViewModel : DirectoryViewModel
    private lateinit var swipeRefreshLayout : SwipeRefreshLayout
    private lateinit var recyclerView : RecyclerView
    private lateinit var adapter: DirectoriesAdapter
    private lateinit var layoutNoItems: View
    private var list : ArrayList<VideoFolderContent> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_directories)

        initView();
    }

    private fun initView() {
        directoryViewModel = ViewModelProvider(this).get(DirectoryViewModel::class.java)
        directoryViewModel.instantiateSharePref(this)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerView)
        layoutNoItems = findViewById(R.id.layoutNoItems)

        initRecyclerView()
        initSwipeListener()
        loadDefaultList();
    }

    override fun onResume() {
        super.onResume()
        reloadDirectoriesList()
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DirectoriesAdapter(this, list)
        recyclerView.adapter = adapter
    }

    private fun initSwipeListener() {
        swipeRefreshLayout.setOnRefreshListener {
            reloadDirectoriesList()
        }
    }

    private fun loadDefaultList()  = runWithPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
        list.addAll(directoryViewModel.getDefaultDirectoryList())
        handleEmptyView()
        if (list.isNotEmpty()) {
            adapter = DirectoriesAdapter(this, list)
            recyclerView.adapter = adapter
        }
    }

    private fun handleEmptyView() {
        if (list.isEmpty()) {
            layoutNoItems.visibility = View.VISIBLE
        } else {
            layoutNoItems.visibility = View.GONE
        }
    }

    private fun reloadDirectoriesList() {
        swipeRefreshLayout.isRefreshing = true
        Thread(Runnable {
            loadDirectories()

            runOnUiThread {
                handleEmptyView()
                if (list.isNotEmpty()) {
                    directoryViewModel.updateDirectoryList(list)
                    adapter = DirectoriesAdapter(this, list)
                    recyclerView.adapter = adapter
                }
                swipeRefreshLayout.isRefreshing = false
            }
        }).start()
    }

    private fun loadDirectories() {
        val videoFolders: ArrayList<VideoFolderContent> = ArrayList()
        videoFolders.addAll(VideoGet.getAllVideoFolders(this, VideoGet.externalContentUri))

        for (videoFolder in videoFolders){
            loadVideos(videoFolder.bucketId, videoFolders)
        }
        list = videoFolders
    }

    private fun loadVideos(bucketId : Int, videoDirectories: ArrayList<VideoFolderContent>) {
        val newList = VideoGet.getAllVideoContentByBucketId(this, bucketId)
        newList.removeAll { !checkFileExists(Uri.parse(it.assetFileStringUri))}
        for (directory in videoDirectories) {
            if (bucketId != 0 && directory.bucketId == bucketId) {
                directory.videoFiles = newList
            }
        }
    }


}