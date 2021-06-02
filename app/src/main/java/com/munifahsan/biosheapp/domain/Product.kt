package com.munifahsan.biosheapp.domain

import com.google.firebase.firestore.DocumentId

class Product(
    @DocumentId
    val id: String = "",
    val nama: String = "",
    val thumbnail: String = "",
    val keterangan: String = "",
    val harga: Int = 0,
    val diskon: Int = 0,
    val stok: Int = 0,
    val berat: Int = 0,
    val sold: Int = 0,
    val selesai: Boolean = false,
    val show: Boolean = false
)
