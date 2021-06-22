package com.sahib.avocado.model

class VideoFolderContent {

    var videoFiles: ArrayList<VideoContent> = ArrayList()
    var folderName: String? = null
    var folderPath: String? = null
    var bucketId: Int = 0

    fun videoFolderContent() {
        videoFiles = ArrayList()
    }

    fun videoFolderContent(
        folderPath: String?,
        folderName: String?
    ) {
        this.folderName = folderName
        this.folderPath = folderPath
        videoFiles = ArrayList()
    }
}
