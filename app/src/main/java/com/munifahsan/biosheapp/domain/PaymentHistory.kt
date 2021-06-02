package com.munifahsan.biosheapp.domain

data class PaymentHistory(
    val biosheOrderId: String = "",
    val midtransOrderId: String = "",
    val midtransTransactionId: String = "",
    val userId: String = "",
    val keterangan: String = "",
    val jumlahPembayaran: Int = 0
)
