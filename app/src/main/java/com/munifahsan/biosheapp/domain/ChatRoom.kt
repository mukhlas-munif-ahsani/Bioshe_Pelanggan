package com.munifahsan.biosheapp.domain

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ChatRoom(
    @DocumentId
    val id: String = "",
    val from: String = "",
    val fromName: String = "",
    val fromPhoto: String = "",
    val to: String = "",
    val toName: String = "",
    val toPhoto: String = "",
    val peakMessage: String = "",
    val fromUnreadChat: Int = 0,
    val toUnreadChat: Int = 0,
    val created: Timestamp? = null,
    val updated: Timestamp? = null,
    val speakers: List<String>? = null
)
