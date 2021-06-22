package com.sahib.avocado.ui.activities

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sahib.avocado.Constants
import com.sahib.avocado.R
import com.sahib.avocado.adapter.VideosAdapter
import com.sahib.avocado.model.VideoContent
import com.sahib.avocado.model.VideoFolderContent
import com.sahib.avocado.utils.VideoGet
import com.sahib.avocado.utils.checkFileExists
import com.sahib.avocado.utils.finishWithFade
import com.sahib.avocado.viewmodel.DirectoryViewModel

class VideosActivity : AppCompatActivity() {

    private lateinit var directoryViewModel: DirectoryViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: Toolbar
    private lateinit var adapter: VideosAdapter
    private lateinit var layoutNoItems: View
    private var bucketId: Int = 0
    private var folderName: String? = ""
    private var list: ArrayList<VideoContent> = ArrayList()
    private var directoriesList: ArrayList<VideoFolderContent> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos)

        initView()
    }

    private fun initView() {
        directoryViewModel = ViewModelProvider(this).get(DirectoryViewModel::class.java)
        directoryViewModel.instantiateSharePref(this)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerView)
        toolbar = findViewById(R.id.toolbar)
        layoutNoItems = findViewById(R.id.layoutNoItems)

        bucketId = intent.getIntExtra(Constants.IntentItems.bucketId.name, 0)
        folderName = intent.getStringExtra(Constants.IntentItems.folderName.name)

        setupToolbar()
        initRecyclerView()
        initSwipeListener()
        loadDefaultList()
    }

    private fun setupToolbar() {
        toolbar.subtitle = folderName
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = VideosAdapter(this, list)
        recyclerView.adapter = adapter
    }

    private fun initSwipeListener() {
        swipeRefreshLayout.setOnRefreshListener {
            reloadVideosList()
        }
    }

    private fun loadDefaultList() {
        val videoDirectories = directoryViewModel.getDefaultDirectoryList()

        for (directory in videoDirectories) {
            if (bucketId != 0 && directory.bucketId == bucketId) {
                list.addAll(directory.videoFiles)
            }
        }

        handleEmptyView()

        if (list.isNotEmpty()) {
            adapter = VideosAdapter(this, list)
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

    private fun reloadVideosList() {
        swipeRefreshLayout.isRefreshing = true
        Thread(Runnable {
            loadVideos()

            runOnUiThread {
                handleEmptyView()
                if (list.isNotEmpty()) {
                    directoryViewModel.updateDirectoryList(directoriesList)
                    handleEmptyView()
                    adapter = VideosAdapter(this, list)
                    recyclerView.adapter = adapter
                }
                swipeRefreshLayout.isRefreshing = false
            }
        }).start()
    }

    private fun loadVideos() {
        if (bucketId != 0) {
            val videoDirectories = directoryViewModel.getDefaultDirectoryList()
            val newList = VideoGet.getAllVideoContentByBucketId(this, bucketId)
            newList.removeAll { !checkFileExists(Uri.parse(it.assetFileStringUri)) }
            for (directory in videoDirectories) {
                if (bucketId != 0 && directory.bucketId == bucketId) {
                    directory.videoFiles = newList
                }
            }

            list = newList
            directoriesList = videoDirectories
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finishWithFade()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
