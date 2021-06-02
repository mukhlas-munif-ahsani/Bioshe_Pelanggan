package com.munifahsan.biosheapp.domain

import com.google.firebase.firestore.DocumentId

class Reward(
    @DocumentId
    val id: String = "",
    val nama: String = "",
    val thumbnail: String = "",
    val points: Int = 0
)
