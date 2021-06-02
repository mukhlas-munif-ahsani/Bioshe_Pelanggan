package com.munifahsan.biosheapp.domain

import com.google.firebase.firestore.DocumentId

data class Promo(
    @DocumentId
    val id: String = "",
    val imageThumbnail: String = ""
)
