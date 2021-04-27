package com.sahib.avocado.model

class VideoContent {

    var videoId: Long = 0
    var videoName: String? = null
    var path: String? = null
    var videoDuration: Long = 0
    var videoSize: Long = 0
    var assetFileStringUri: String? = null
    var album: String? = null
    var artist: String? = null

    fun VideoContent(
        videoId: Long,
        videoName: String?,
        path: String?,
        videoDuration: Long,
        videoSize: Long
    ) {
        this.videoId = videoId
        this.videoName = videoName
        this.path = path
        this.videoDuration = videoDuration
        this.videoSize = videoSize
    }
    
}