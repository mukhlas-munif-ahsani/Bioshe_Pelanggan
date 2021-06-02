package com.munifahsan.biosheapp.domain

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Chat(
    @DocumentId
    val id: String = "",
    val message: String = "",
    @ServerTimestamp
    var send: Date? = null,
    val seen: Boolean? = null,
    val sender: String = "",
    val receiver: String = ""
)
