package com.munifahsan.biosheapp.domain

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val id: String = "",
    val nama: String = "",
    val email: String = "",
    val level: String = "",
    val photo_url: String = "",
    val nik: String = "",
    val gender: String = "",
    val alamatRumah: String = "",
    val noHp: String = "",
    val noWa: String = "",
    val ahliWaris: String = "",
    val namaOutlet: String = "",
    val alamatOutlet: String = "",
    val salesId: String = "",
    val keranjangTotalBarang: Int = 0,
    val keranjangTotalHarga: Int = 0,
    val loyalti: String = ""
) {

}
