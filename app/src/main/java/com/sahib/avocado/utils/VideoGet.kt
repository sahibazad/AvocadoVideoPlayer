package com.sahib.avocado.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.sahib.avocado.model.VideoContent
import com.sahib.avocado.model.VideoFolderContent

object VideoGet {

    val externalContentUri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    val internalContentUri: Uri = MediaStore.Video.Media.INTERNAL_CONTENT_URI
    val FILE_TYPE_NO_MEDIA: String = ".nomedia"

    @SuppressLint("InlinedApi")
    var projections = arrayOf(
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Video.Media.BUCKET_ID,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.ALBUM,
        MediaStore.Video.Media.DATE_TAKEN,
        MediaStore.Video.Media.ARTIST
    )

    /**Returns an Arraylist of [VideoContent] */
    fun getAllVideoContent(context:Context, contentLocation: Uri?) : ArrayList<VideoContent> {
        val allVideo: ArrayList<VideoContent> = ArrayList()
        context.contentResolver.query(
            externalContentUri,
            projections,
            null,
            null,
            "LOWER (" + MediaStore.Video.Media.DATE_TAKEN + ") DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val videoContent = VideoContent()
                videoContent.videoName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
                videoContent.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                videoContent.videoDuration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
                videoContent.videoSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))
                videoContent.videoId = (cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))).toLong()
                videoContent.assetFileStringUri = (Uri.withAppendedPath(contentLocation, videoContent.videoId.toString())).toString()
                videoContent.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM))
                videoContent.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST))
                allVideo.add(videoContent)
            }
        }

        return allVideo
    }

    /**Returns an Arraylist of [VideoContent] in a specific folder   */
    fun getAllVideoContentByBucketId(context: Context, bucket_id: Int): ArrayList<VideoContent> {
        val allVideo: ArrayList<VideoContent> = ArrayList()
        context.contentResolver.query(
            externalContentUri,
            projections,
            MediaStore.Video.Media.BUCKET_ID + " like ? ",
            arrayOf("%$bucket_id%"),
            "LOWER (" + MediaStore.Video.Media.DATE_TAKEN + ") DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val videoContent = VideoContent()
                videoContent.videoName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
                videoContent.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                videoContent.videoDuration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
                videoContent.videoSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))
                videoContent.videoId = (cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))).toLong()
                videoContent.assetFileStringUri = (Uri.withAppendedPath(externalContentUri, videoContent.videoId.toString())).toString()
                videoContent.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM))
                videoContent.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST))
                allVideo.add(videoContent)
            }
        }

        return allVideo
    }

    /**Returns an Arraylist of [VideoFolderContent] with each videoFolderContent having an Arraylist of all it VideoContent */
    fun getAllVideoFolders(context: Context, contentLocation: Uri): ArrayList<VideoFolderContent> {
        val allVideoFolders: ArrayList<VideoFolderContent> = ArrayList()
        val videoPaths: ArrayList<Int> = ArrayList()
        context.contentResolver.query(
            contentLocation,
            projections,
            null,
            null,
            "LOWER (" + MediaStore.Video.Media.DATE_TAKEN + ") DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val videoFolder = VideoFolderContent()
                val videoContent = VideoContent()
                videoContent.videoName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
                videoContent.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                videoContent.videoDuration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
                videoContent.videoSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))
                videoContent.videoId = (cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))).toLong()
                videoContent.assetFileStringUri = (Uri.withAppendedPath(externalContentUri, videoContent.videoId.toString())).toString()
                videoContent.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM))
                videoContent.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST))

                val folder: String = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                val datapath: String = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                val bucketId: Int = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID))
                var folderpaths = datapath.substring(0, datapath.lastIndexOf("$folder/"))
                folderpaths = "$folderpaths$folder/"
                if (!videoPaths.contains(bucketId)) {
                    videoPaths.add(bucketId)
                    videoFolder.bucketId = bucketId
                    videoFolder.folderPath = folderpaths
                    videoFolder.folderName = folder
                    videoFolder.videoFiles.add(videoContent)
                    allVideoFolders.add(videoFolder)
                } else {
                    for (i in 0 until allVideoFolders.size) {
                        if (allVideoFolders[i].bucketId == bucketId) {
                            allVideoFolders[i].videoFiles.add(videoContent)
                        }
                    }
                }
            }
        }

        return allVideoFolders
    }

}