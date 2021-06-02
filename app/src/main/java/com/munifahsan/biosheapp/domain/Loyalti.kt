package com.munifahsan.biosheapp.domain

import android.graphics.drawable.Drawable
import com.google.firebase.firestore.DocumentId

data class Loyalti(
    @DocumentId
    var id: String = "",
    var nama: String = "",
    var icon: Drawable,
    val backgroundColor: Int = 0
)
