package com.sahib.avocado

class Constants {

    enum class SharedPrefNames {
        general,
        directories,
        directoriesWithNoMedia,
        videoStatus
    }

    enum class SharedPrefItemNames {
        isOnboarded,
        list,
        position,
        brightness
    }

    enum class IntentItems {
        bucketId,
        folderName,
        videoUri,
        videoName,
        videoDuration,
        videoList,
        position
    }

}