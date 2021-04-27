package com.sahib.avocado.adapter

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sahib.avocado.Constants
import com.sahib.avocado.R
import com.sahib.avocado.model.VideoFolderContent
import com.sahib.avocado.ui.activities.VideosActivity
import com.sahib.avocado.utils.inflate
import com.sahib.avocado.utils.startActivityWithFade

class DirectoriesAdapter(private val context : Context, private val list : ArrayList<VideoFolderContent>) : RecyclerView.Adapter<DirectoriesAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirectoriesAdapter.ViewHolder {
        val inflatedView = parent.inflate(R.layout.item_directory, false)
        return DirectoriesAdapter.ViewHolder(inflatedView)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: DirectoriesAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.bindDirectory(item)
    }

    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private var directory : VideoFolderContent? = null

        init {
            view.setOnClickListener(this)
        }

        fun bindDirectory(directory: VideoFolderContent) {
            this.directory = directory
            view.findViewById<TextView>(R.id.text_directory).text = directory.folderName
            view.findViewById<TextView>(R.id.text_items).text = directory.videoFiles!!.size.toString() + " " + view.context.getString(R.string.items)
        }

        override fun onClick(v: View) {
            val context = itemView.context
            val intent = Intent(context, VideosActivity::class.java)
            intent.putExtra(Constants.IntentItems.bucketId.name, directory?.bucketId)
            intent.putExtra(Constants.IntentItems.folderName.name, directory?.folderName)
            context.startActivityWithFade(intent)
        }
    }
}