package com.munifahsan.biosheapp.domain

data class Users(
    val nama: String = "",
    val namaBelakang: String = "",
    val email: String = "",
    val level: String = "",
    val photo_url: String = "",
    val nik: String = "",
    val gender: String = "",
    val alamatRumah: String = "",
    val noHp: String = "",
    val noWa: String = "",
    val ahliWaris: String = "",
    val kota: String = "",
    val kodePos:String = "",
    val kodeKota: String = "",
    val namaOutlet: String = "",
    val alamatOutlet: String = "",
    val salesId: String = "",
    val keranjangTotalBarang: Int = 0,
    val keranjangTotalHarga: Int = 0,
    val bioshePoints: Int = 0,
    val loyalti: String = "",
    val jumlahPesanan: Int = 0
) {

}
