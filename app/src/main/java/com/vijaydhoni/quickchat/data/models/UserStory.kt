package com.vijaydhoni.quickchat.data.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserStory(
    var user: User? = null,
    var lastUpdatedTime: Timestamp? = null,
    var stories: List<Story> = emptyList()
) : Parcelable

@Parcelize
data class Story(
    var storyImageUrl: String = "",
    var storyID: String? = null,
    var userID: String? = null,
    var storySeenBy: List<String> = emptyList(),
    var storyCreatedTimestamp: Timestamp? = Timestamp.now()
) : Parcelable


