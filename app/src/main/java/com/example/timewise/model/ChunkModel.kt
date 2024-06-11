package com.example.timewise.model

import java.io.Serializable

data class ChunkModel (
    var id: Int = 0,
    var userId: String? = null,
    var title: String? = null,
    var activityName: String? = null,
    var activityPicture: String? = null,
    var date: String? = null,
    var startTime: String? = null,
    var endTime: String? = null,
    var isReminder: String? = null,
    var isRepeat: String? = null,
    var notes: String? = null,
    var tags: ArrayList<String> = ArrayList(),
    var images: ArrayList<String> = ArrayList(),
): Serializable