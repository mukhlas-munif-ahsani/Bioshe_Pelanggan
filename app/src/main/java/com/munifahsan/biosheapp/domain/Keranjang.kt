package com.munifahsan.biosheapp.domain

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Keranjang(
    @DocumentId
    val id: String = "",
    val productId: String = "",
    val createdDate: Timestamp? = null,
    val jumlahItem: Int = 0,
    val nama: String = "",
    val thumbnail: String = "",
    val harga: Int = 0,
    val diskon: Int = 0,
    val berat: Int = 0
//    val bought: Boolean
)
