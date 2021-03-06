package com.sahib.avocado.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sahib.avocado.Constants
import com.sahib.avocado.R
import com.sahib.avocado.model.VideoContent
import com.sahib.avocado.ui.activities.VideoPlayerActivity
import com.sahib.avocado.utils.inflate
import com.sahib.avocado.utils.startActivityWithFade

class VideosAdapter(private val context : Context, private val list : ArrayList<VideoContent>) : RecyclerView.Adapter<VideosAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosAdapter.ViewHolder {
        val inflatedView = parent.inflate(R.layout.item_video, false)
        return VideosAdapter.ViewHolder(context, inflatedView)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VideosAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.bindVideoFile(item, position, list)
    }

    class ViewHolder(private val context : Context, private val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private var videoFile : VideoContent? = null
        private var position: Int? = null
        private var list: ArrayList<VideoContent>? = null

        init {
            view.setOnClickListener(this)
        }

        fun bindVideoFile(
            videoFile: VideoContent,
            position: Int,
            list: ArrayList<VideoContent>
        ) {
            this.videoFile = videoFile
            this.position = position
            this.list = list

            Glide.with(context).load(Uri.parse(videoFile.assetFileStringUri))
                .centerCrop()
                .into(view.findViewById<ImageView>(R.id.image_video))
            view.findViewById<TextView>(R.id.text_video).text = videoFile.videoName
        }

        override fun onClick(v: View) {
            val context = itemView.context
//            val intent = Intent(context, VideoPlayerVDKActivity::class.java)
            val intent = Intent(context, VideoPlayerActivity::class.java)
//            intent.putExtra(Constants.IntentItems.videoName.name, videoFile?.videoName)
//            intent.putExtra(Constants.IntentItems.videoUri.name, videoFile?.assetFileStringUri)
//            intent.putExtra(Constants.IntentItems.videoDuration.name, videoFile?.videoDuration)
            intent.putParcelableArrayListExtra(Constants.IntentItems.videoList.name, list)
            intent.putExtra(Constants.IntentItems.position.name, position)
            context.startActivityWithFade(intent)
        }
    }
}